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
package uk.gov.gchq.maestro.executor.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.maestro.commonutil.ToStringBuilder;
import uk.gov.gchq.maestro.executor.Context;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.OperationChain;
import uk.gov.gchq.maestro.operation.user.User;

/**
 * A {@code Request} is a request that will be executed on a Maestro
 * {@link uk.gov.gchq.maestro.executor.Executor}.
 * A new {@link Context} with new jobId will be created based on your {@link Context}/{@link User}.
 *
 * @param <O> the result type of the request.
 */
public class Request<O> {
    private Operation operation;
    private final Context context;
    private Config config;

    public Request(final Operation operation, final User user) {
        if (null == operation) {
            throw new IllegalArgumentException("An operation is required");
        }
        if (null == user) {
            throw new IllegalArgumentException("A user is required");
        }
        this.operation = operation;
        this.context = new Context(user);
    }

    @JsonCreator
    public Request(@JsonProperty("operationChain") final Operation operation, @JsonProperty("context") final Context context) {
        if (null == operation) {
            throw new IllegalArgumentException("An operation is required");
        }
        if (null == context) {
            throw new IllegalArgumentException("A context containing a user is required");
        }
        this.operation = operation;
        this.context = context;
    }

    /**
     * @return the OperationChain stored within the storeRequest
     * @deprecated Use getOperation instead.
     */
    public OperationChain getOperationChain() {
        return (OperationChain) operation;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(final Operation operation) {
        this.operation = operation;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }

    public Context getContext() {
        return context;
    }

    public Request<O> fullClone() {
        return new Request(operation.shallowClone(), context.shallowClone());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Request<?> that = (Request<?>) o;

        return new EqualsBuilder()
                .append(operation, that.operation)
                .append(context, that.context)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(operation)
                .append(context)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("operation", operation)
                .append("context", context)
                .toString();
    }
}
