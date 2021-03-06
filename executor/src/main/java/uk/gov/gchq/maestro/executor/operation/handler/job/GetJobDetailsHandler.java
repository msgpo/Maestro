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

package uk.gov.gchq.maestro.executor.operation.handler.job;


import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.executor.Context;
import uk.gov.gchq.maestro.executor.Executor;
import uk.gov.gchq.maestro.executor.operation.declaration.FieldDeclaration;
import uk.gov.gchq.maestro.executor.operation.handler.OutputOperationHandler;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.jobtracker.JobDetail;
import uk.gov.gchq.maestro.operation.jobtracker.JobTracker;

import static uk.gov.gchq.maestro.commonutil.exception.Status.SERVICE_UNAVAILABLE;

/**
 * A {@code GetJobDetailsHandler} handles GetJobDetails operations by querying
 * the configured store's job tracker for the required job details.
 */
public class GetJobDetailsHandler implements OutputOperationHandler<JobDetail> {
    @Override
    public JobDetail _doOperation(final Operation /*GetJobDetails*/ operation,
                                  final Context context, final Executor executor) throws OperationException {
        if (!JobTracker.isCacheEnabled()) {
            throw new OperationException("The Job Tracker has not been configured", SERVICE_UNAVAILABLE);
        }
        final String j = (String) operation.get("JobId");
        final String jobId = null != j ? j : context.getJobId();
        return JobTracker.getJob(jobId);
    }

    @Override
    public FieldDeclaration getFieldDeclaration() {
        return new FieldDeclaration().fieldRequired("JobId", String.class);
    }

}
