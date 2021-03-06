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

package uk.gov.gchq.maestro.executor;

import org.junit.Test;

import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.commonutil.exception.SerialisationException;
import uk.gov.gchq.maestro.executor.helper.TestHandler;
import uk.gov.gchq.maestro.executor.operation.declaration.FieldDeclaration;
import uk.gov.gchq.maestro.executor.operation.handler.OperationHandler;
import uk.gov.gchq.maestro.executor.util.Config;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.helper.MaestroObjectTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.gov.gchq.maestro.executor.Executor.INITIALISER;

public class ExecutorTest extends MaestroObjectTest<Executor> {

    @Override
    protected String getJSONString() {
        return "{\n" +
                "  \"class\" : \"uk.gov.gchq.maestro.executor.Executor\",\n" +
                "  \"config\" : {\n" +
                "    \"class\" : \"uk.gov.gchq.maestro.executor.util.Config\",\n" +
                "    \"operationHandlers\" : {\n" +
                "      \"testOperation\" : {\n" +
                "        \"class\" : \"uk.gov.gchq.maestro.executor.helper.TestHandler\",\n" +
                "        \"fieldDeclaration\" : {\n" +
                "          \"class\" : \"uk.gov.gchq.maestro.executor.operation.declaration.FieldDeclaration\",\n" +
                "          \"fields\" : {\n" +
                "            \"field\" : \"java.lang.String\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"handlerField\" : \"handlerFieldValue1\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"properties\" : {\n" +
                "      \"configKey\" : \"configValue\"\n" +
                "    },\n" +
                "    \"defaultHandler\" : {\n" +
                "      \"class\" : \"uk.gov.gchq.maestro.executor.operation.handler.DefaultHandler\"\n" +
                "    },\n" +
                "    \"operationHooks\" : [ ],\n" +
                "    \"requestHooks\" : [ ]\n" +
                "  }\n" +
                "}";
    }

    @Override
    protected Executor getFullyPopulatedTestObject() {
        final Config config = new Config();
        final Map<String, Object> properties = new HashMap<>();
        properties.put("configKey", "configValue");
        config.setProperties(properties);
        config.addOperationHandler("testOperation", new TestHandler().handlerField("handlerFieldValue1"));
        return new Executor(config);
    }

    @Test
    public void shouldRunTestHandler() throws SerialisationException, OperationException {
        final Executor executor = getFullyPopulatedTestObject();
        final Object execute = executor.execute(new Operation("TestOperation").operationArg("field", "opFieldValue1"), new Context());
        assertEquals("handlerFieldValue1,opFieldValue1", execute);
    }

    @Test
    public void shouldUseInitialiserHandlerUsingConfigBuilderMethod() {
        // Given
        final Config config = new Config();
        config.addOperationHandler(INITIALISER, new InitialiserHandlerImpl());

        // When / Then
        try {
            new Executor(config);
            fail("Exception expected");
        } catch (final Exception e) {
            try {
                assertTrue(e.getCause().getMessage().contains("Thrown within InitialiserHandlerImpl"));
            } catch (final Exception e2) {
                throw e;
            }
        }
    }

    @Test
    public void shouldUseInitialiserHandlerUsingConstructor() {
        // Given
        final Config config = new Config();
        config.addOperationHandler(INITIALISER, new InitialiserHandlerImpl());

        // When / Then
        try {
            new Executor(config);
            fail("Exception expected");
        } catch (final Exception e) {
            try {
                assertTrue(e.getCause().getMessage().contains("Thrown within InitialiserHandlerImpl"));
            } catch (final Exception e2) {
                throw e;
            }
        }
    }

    /*  @Test
      public void shouldRestartAndInitialiseJobsUsingCacheService() throws OperationException {
          Properties properties = new Properties();
          ExecutorPropertiesUtil.setCacheClass(properties, "uk.gov.gchq.maestro.cache.impl.JcsCacheService");
          properties.setProperty("gaffer.cache.config.file", "resources/cache.ccf");
          ExecutorPropertiesUtil.setJobTrackerEnabled(properties, true);
          Config config = new Config.Builder().executorProperties(properties).build();

          Executor executor = new Executor(config);
          Context context = new Context();
          executor.execute(new Job.Builder().operation(new ToList<>()).build(), context);
      }
  */

    @Override
    protected Class<Executor> getTestObjectClass() {
        return Executor.class;
    }

    private class InitialiserHandlerImpl implements OperationHandler {

        @Override
        public Object _doOperation(final Operation operation, final Context context, final Executor executor) throws OperationException {
            throw new OperationException("Thrown within InitialiserHandlerImpl");
        }

        @Override
        public FieldDeclaration getFieldDeclaration() {
            return new FieldDeclaration();
        }
    }

    @Test
    public void shouldIsSupportedCapitalInsensitive() {
        assertTrue(getFullyPopulatedTestObject().isSupported("testOperation"));
        assertTrue(getFullyPopulatedTestObject().isSupported("TESTOPERATION"));
        assertTrue(getFullyPopulatedTestObject().isSupported("testoperation"));
    }
}
