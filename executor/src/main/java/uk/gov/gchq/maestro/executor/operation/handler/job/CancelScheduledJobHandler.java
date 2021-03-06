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
import uk.gov.gchq.maestro.executor.operation.handler.OperationHandler;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.jobtracker.JobStatus;
import uk.gov.gchq.maestro.operation.jobtracker.JobTracker;

public class CancelScheduledJobHandler implements OperationHandler {
    @Override
    public Void _doOperation(final Operation /*CancelScheduledJob*/ operation,
                             final Context context, final Executor executor) throws OperationException {
        if (!JobTracker.isCacheEnabled()) {
            throw new OperationException("JobTracker not enabled");
        }
        final String o = (String) operation.get("JobId");
        if (null == o) {
            throw new OperationException("job id must be specified");
        }

        if (JobTracker.getJob(o).getStatus().equals(JobStatus.SCHEDULED_PARENT)) {
            JobTracker.getJob(o).setStatus(JobStatus.CANCELLED);
        } else {
            throw new OperationException("Job with jobId: " + o + " is not a scheduled job and cannot be cancelled.");
        }
        return null;
    }

    @Override
    public FieldDeclaration getFieldDeclaration() {
        return new FieldDeclaration().fieldRequired("JobId", String.class);
    }
}
