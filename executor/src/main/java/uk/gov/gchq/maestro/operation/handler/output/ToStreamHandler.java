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
package uk.gov.gchq.maestro.operation.handler.output;

import uk.gov.gchq.maestro.Context;
import uk.gov.gchq.maestro.Executor;
import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.commonutil.stream.Streams;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.fields.FieldsUtil;
import uk.gov.gchq.maestro.operation.handler.OutputOperationHandler;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A {@code ToStreamHandler} handles for ToStream operations.
 * <p>
 * Simply wraps the operation input items into a {@link Stream}
 * for further processing.
 */
public class ToStreamHandler<T> implements OutputOperationHandler<Stream<? extends T>> {
    @Override
    public Stream<? extends T> doOperation(final Operation/*ToStream<T>*/ operation,
                                           final Context context,
                                           final Executor executor) throws OperationException {
        Arrays.stream(Fields.values()).forEach(f -> f.validate(operation));
        //TODO Logic allows for null but Above validation will throw a uk.gov.gchq.maestro.commonutil.exception.MaestroRuntimeException
        final Iterable<T> input = (Iterable<T>) operation.input();
        if (null == input) {
            return null;
        }

        return Streams.toStream(input);
    }

    public enum Fields {
        Input(Iterable.class);

        Class instanceOf;

        Fields() {
            this(Object.class);
        }

        Fields(final Class instanceOf) {
            this.instanceOf = instanceOf;
        }

        public void validate(Operation operation) {
            FieldsUtil.validate(this, operation, instanceOf);
        }

        public Object get(Operation operation) {
            return FieldsUtil.get(operation, this);
        }
    }


}
