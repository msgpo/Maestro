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

package uk.gov.gchq.maestro.executor.operation.handler.named;


import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.executor.Context;
import uk.gov.gchq.maestro.executor.Executor;
import uk.gov.gchq.maestro.executor.operation.declaration.FieldDeclaration;
import uk.gov.gchq.maestro.executor.operation.handler.OutputOperationHandler;
import uk.gov.gchq.maestro.operation.Operation;

/**
 * Operation handler for . Named operations are resolved by
 * the {@code NamedOperationResolver} {@code GraphHook}.
 * <p>
 * If this handler is invoked then it means the named operation could not be resolved.
 */
public class NamedOperationHandler implements OutputOperationHandler {
    @Override
    public Object _doOperation(final Operation operation,
                               final Context context, final Executor executor) throws OperationException {
        throw new UnsupportedOperationException("The named operation: " + operation.get("OperationName") + " was not found.");
    }

    @Override
    public FieldDeclaration getFieldDeclaration() {
        return new FieldDeclaration().field("OperationName", String.class);
    }
}
