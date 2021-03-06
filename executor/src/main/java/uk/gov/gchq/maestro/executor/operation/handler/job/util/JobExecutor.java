/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.maestro.executor.operation.handler.job.util;

import uk.gov.gchq.maestro.commonutil.ExecutorService;
import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.executor.Context;
import uk.gov.gchq.maestro.executor.Executor;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.OperationChain;
import uk.gov.gchq.maestro.operation.Operations;
import uk.gov.gchq.maestro.operation.jobtracker.JobDetail;
import uk.gov.gchq.maestro.operation.jobtracker.JobStatus;
import uk.gov.gchq.maestro.operation.jobtracker.JobTracker;

/**
 * This class is used within the {@link uk.gov.gchq.maestro.executor.operation.handler.job.JobHandler} and {@link Executor} to
 * execute (scheduled and non-scheduled)Jobs using
 * the {@link JobTracker} and {@link ExecutorService}.
 */
public final class JobExecutor {

    private JobExecutor() {
        // private to prevent instantiation
    }

    public static JobDetail executeJob(final JobDetail jobDetail,
                                       final Context context, final Executor executor) throws OperationException {
        if (!JobTracker.isCacheEnabled()) {
            throw new OperationException("JobTracker has not been configured.");
        }

        if (null == ExecutorService.getService() || !ExecutorService.isEnabled()) {
            throw new OperationException(("Executor Service is not enabled."));
        }

        if (null != jobDetail.getRepeat()) {
            return scheduleJob(jobDetail, context, executor);
        } else {
            return runJob(jobDetail, context, executor);
        }
    }

    public static JobDetail executeJob(final Operation operation,
                                       final Context context,
                                       final String parentJobId,
                                       final Executor executor) throws OperationException {
        JobDetail childJobDetail = addOrUpdateJobDetail(operation, context, null, JobStatus.RUNNING);
        childJobDetail.setParentJobId(parentJobId);
        return executeJob(childJobDetail, context, executor);
    }

    /**
     * executes a  Job as a parentJob.
     * NOTE - this will create a new {@link Context}.
     *
     * @param parentJobDetail The parent {@link JobDetail}
     * @param executor        The {@link Executor} instance
     * @return The child {@link JobDetail}
     * @throws OperationException If exception executing job
     */
    public static JobDetail executeJob(final JobDetail parentJobDetail, final Executor executor) throws OperationException {
        final Context context = new Context();
        JobDetail childJobDetail = addOrUpdateJobDetail(parentJobDetail.getOpAsOperation(), context, null, JobStatus.RUNNING);
        childJobDetail.setParentJobId(parentJobDetail.getParentJobId());
        return executeJob(childJobDetail, context, executor);
    }

    public static JobDetail scheduleJob(final JobDetail parentJobDetail,
                                        final Context context, final Executor executor) {
        executor.getExecutorService().scheduleAtFixedRate(() -> {
            if ((JobTracker.getJob(parentJobDetail.getJobId()).getStatus().equals(JobStatus.CANCELLED))) {
                Thread.currentThread().interrupt();
                return;
            }
            final Operation operation =
                    parentJobDetail.getOpAsOperation().shallowClone();
            final Context newContext = context.shallowClone();
            try {
                executeJob(operation, newContext, parentJobDetail.getJobId(),
                        executor);
            } catch (final OperationException e) {
                throw new RuntimeException("Exception within scheduled job", e);
            }
        }, parentJobDetail.getRepeat().getInitialDelay(), parentJobDetail.getRepeat().getRepeatPeriod(), parentJobDetail.getRepeat().getTimeUnit());

        return addOrUpdateJobDetail(parentJobDetail.getOpAsOperation(), context, null, JobStatus.SCHEDULED_PARENT);
    }

    public static JobDetail runJob(final JobDetail jobDetail, final Context context, final Executor executor) {
        Operation operation = jobDetail.getOpAsOperation();
        final OperationChain opChain;

        if (operation instanceof Operations) {
            opChain = (OperationChain) operation;
        } else {
            opChain = OperationChain.wrap(operation.getId(), operation);
        }

        if (executor.isSupported("ExportToResultCache")) {
            boolean hasExport = false;

            for (final Operation op : opChain.getOperations()) {
                if (op.getIdComparison("ExportToResultCache")) {
                    hasExport = true;
                    break;
                }
            }
            if (!hasExport) {
                opChain.getOperations()
                        .add(new Operation("ExportToResultCache"));
            }
        }

        executor.runAsync(() -> {
            try {
                executor.execute(opChain, context);
                addOrUpdateJobDetail(opChain, context, null,
                        JobStatus.FINISHED);
            } catch (final Error e) {
                addOrUpdateJobDetail(opChain, context, e.getMessage(),
                        JobStatus.FAILED);
                throw e;
            } catch (final Exception e) {
                addOrUpdateJobDetail(opChain, context, e.getMessage(),
                        JobStatus.FAILED);
            }
        });
        return jobDetail;
    }

    public static JobDetail addOrUpdateJobDetail(final Operation operation,
                                                 final Context context, final String msg, final JobStatus jobStatus) {
        final JobDetail newJobDetail = new JobDetail(context.getJobId(), context
                .getUser()
                .getUserId(), OperationChain.wrap(operation.getId(), operation), jobStatus, msg);
        if (JobTracker.isCacheEnabled()) {
            final JobDetail oldJobDetail =
                    JobTracker.getJob(newJobDetail.getJobId());
            if (null == oldJobDetail) {
                JobTracker.addOrUpdateJob(newJobDetail);
            } else {
                JobTracker.addOrUpdateJob(new JobDetail(oldJobDetail, newJobDetail));
            }
        }
        return newJobDetail;
    }
}
