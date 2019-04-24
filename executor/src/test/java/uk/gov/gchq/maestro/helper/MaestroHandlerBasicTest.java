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

package uk.gov.gchq.maestro.helper;

import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.maestro.Context;
import uk.gov.gchq.maestro.Executor;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.handler.OperationHandler;
import uk.gov.gchq.maestro.user.User;
import uk.gov.gchq.maestro.util.Config;

import static org.junit.Assert.fail;

/**
 * Abstract test to perform the initial basic form of testing for a handler.
 *
 * @param <Op> The Operation
 * @param <H>  The Handler
 * @see OperationHandler
 */
public abstract class MaestroHandlerBasicTest<Op extends Operation, H extends OperationHandler<Op>> {
    public static final String EXECUTOR_ID = "testExecutorId";
    protected Executor testExecutor;
    protected Context context;
    protected User testUser;

    @Before
    public void setUp() throws Exception {
        testUser = new User("testUser");
        context = getContext();
        testExecutor = getTestExecutor();
    }

    @Test
    public void shouldHandleABasicExample() throws Exception {
        final Op op = getBasicOp();
        final H handler = getBasicHandler();
        final Object handlerValue = handler.doOperation(op, context, testExecutor);
        inspectReturnFromHandler(handlerValue);
        inspectFields();
    }

    @Test
    public void shouldExecuteABasicExample() throws Exception {
        final Op op = getBasicOp();
        final Object executeValue = testExecutor.execute(op, context);
        inspectReturnFromExecute(executeValue);
        inspectFields();
    }

    protected abstract H getBasicHandler() throws Exception;

    protected abstract Op getBasicOp() throws Exception;

    protected void inspectFields() throws Exception {
        fail("Override test method: inspectFields");
    }

    protected Executor getTestExecutor() throws Exception {
        return new Executor().config(new Config(EXECUTOR_ID));
    }

    protected Context getContext() {
        return new Context(testUser);
    }

    protected void inspectReturnFromHandler(final Object value) throws Exception {
        fail("Override test method: inspectReturnFromHandler");
    }

    protected void inspectReturnFromExecute(final Object value) throws Exception {
        fail("Override test method: inspectReturnFromExecute");
    }
}
