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

package uk.gov.gchq.maestro.operation.handler.named;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.gov.gchq.maestro.Context;
import uk.gov.gchq.maestro.Executor;
import uk.gov.gchq.maestro.commonutil.exception.CacheOperationException;
import uk.gov.gchq.maestro.commonutil.exception.OperationException;
import uk.gov.gchq.maestro.commonutil.exception.SerialisationException;
import uk.gov.gchq.maestro.commonutil.iterable.WrappedCloseableIterable;
import uk.gov.gchq.maestro.commonutil.serialisation.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.maestro.helper.MaestroHandlerBasicTest;
import uk.gov.gchq.maestro.named.operation.NamedOperationDetail;
import uk.gov.gchq.maestro.named.operation.ParameterDetail;
import uk.gov.gchq.maestro.operation.Operation;
import uk.gov.gchq.maestro.operation.OperationChain;
import uk.gov.gchq.maestro.operation.handler.named.cache.NamedOperationCache;
import uk.gov.gchq.maestro.user.User;
import uk.gov.gchq.maestro.util.Config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class AddNamedOperationHandlerTest {
    private static final String EMPTY_ADMIN_AUTH = "";
    private final NamedOperationCache mockCache = mock(NamedOperationCache.class);
    private final AddNamedOperationHandler handler = new AddNamedOperationHandler(mockCache);
    public static final String USER = "User";
    private Context context = new Context(new User.Builder()
            .userId("test user")
            .build());
    private Executor executor = mock(Executor.class);

    private Operation addNamedOperation = new Operation("addNamedOperation")
            .operationArg("overwrite", false);
    private static final String OPERATION_NAME = "test";
    private HashMap<String, NamedOperationDetail> storedOperations = new HashMap<>();

    @Before
    public void before() throws CacheOperationException {
        storedOperations.clear();
        addNamedOperation.operationArg("OperationName", (OPERATION_NAME));

        doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            storedOperations.put(((NamedOperationDetail) args[0]).getOperationName(), (NamedOperationDetail) args[0]);
            return null;
        }).when(mockCache).addNamedOperation(any(NamedOperationDetail.class), anyBoolean(), any(User.class), eq(EMPTY_ADMIN_AUTH));

        doAnswer(invocationOnMock ->
                new WrappedCloseableIterable<>(storedOperations.values()))
                .when(mockCache).getAllNamedOperations(any(User.class), eq(EMPTY_ADMIN_AUTH));

        doAnswer(invocationOnMock -> {
            String name = (String) invocationOnMock.getArguments()[0];
            NamedOperationDetail result = storedOperations.get(name);
            if (result == null) {
                throw new CacheOperationException("failed");
            }
            return result;
        }).when(mockCache).getNamedOperation(anyString(), any(User.class), eq(EMPTY_ADMIN_AUTH));

        given(executor.getConfig()).willReturn(new Config());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @After
    public void after() throws CacheOperationException {
        addNamedOperation.operationArg("OperationName", null);
        addNamedOperation.operationArg("OperationChain", (String) null);
        addNamedOperation.operationArg("Description", null);
        addNamedOperation.operationArg("OverwriteFlag", false);
        mockCache.clear();
    }


    @Test
    public void shouldNotAllowForNonRecursiveNamedOperationsToBeNested() throws OperationException {
        OperationChain child =
                new OperationChain("opChain", new Operation("ToArray"));
        addNamedOperation.operationArg("OperationChain", child);
        addNamedOperation.operationArg("OperationName", "child");
        handler.doOperation(addNamedOperation, context, executor);

        OperationChain parent = new OperationChain("opchain", Lists.newArrayList(
                new Operation("NamedOperation").operationArg("name", "child"),
                new Operation("ToArray")));

        addNamedOperation.operationArg("OperationChain", parent);
        addNamedOperation.operationArg("OperationName", "parent");

        exception.expect(OperationException.class);

        handler.doOperation(addNamedOperation, context, executor);
    }

    @Test
    public void shouldAllowForOperationChainJSONWithParameter() {
        try {
            final String opChainJSON = "{ \"operations\": [ { \"class\":\"uk" +
                    ".gov.gchq.maestro.operation.impl.output" +
                    ".ToSingletonList\", " +
                    "\"input\" : \"${param1}\"}] }";

            addNamedOperation.operationArg("OperationChain", opChainJSON);
            addNamedOperation.operationArg("OperationName", "namedop");
            ParameterDetail param = new ParameterDetail.Builder()
                    .defaultValue(1L)
                    .description("Limit param")
                    .valueClass(Long.class)
                    .build();
            Map<String, ParameterDetail> paramMap = Maps.newHashMap();
            paramMap.put("param1", param);
            addNamedOperation.operationArg("Parameters", paramMap);
            handler.doOperation(addNamedOperation, context, executor);
            assert cacheContains("namedop");

        } catch (final Exception e) {
            fail("Expected test to pass without error. Exception " + e.getMessage());
        }

    }

    @Test
    public void shouldNotAllowForOperationChainWithParameterNotInOperationString() throws OperationException {
        final String opChainJSON = "{ \"operations\": [ { \"class\":\"uk" +
                ".gov.gchq.maestro.operation.impl.output" +
                ".ToSingletonList\", " +
                "\"input\" : \"${param1}\"}] }";

        addNamedOperation.operationArg("OperationChain", opChainJSON);
        addNamedOperation.operationArg("OperationName", "namedop");

        // Note the param is String class to get past type checking which will also catch a param
        // with an unknown name if its not a string.
        ParameterDetail param = new ParameterDetail.Builder()
                .defaultValue("setKey")
                .description("key param")
                .valueClass(String.class)
                .build();
        Map<String, ParameterDetail> paramMap = Maps.newHashMap();
        paramMap.put("param2", param);
        addNamedOperation.operationArg("Parameters", paramMap);

        exception.expect(OperationException.class);
        handler.doOperation(addNamedOperation, context, executor);
    }

    @Test
    public void shouldNotAllowForOperationChainJSONWithInvalidParameter() throws SerialisationException {
        String opChainJSON = "{" +
                "  \"operations\": [" +
                "      {" +
                "          \"class\": \"uk.gov.gchq.maestro.named.operation.AddNamedOperation\"," +
                "          \"operationName\": \"testInputParam\"," +
                "          \"overwriteFlag\": true," +
                "          \"operationChain\": {" +
                "              \"operations\": [" +
                "                  {" +
                "                      \"class\": \"uk.gov.gchq.maestro.operation.impl.output.ToSingletonList\", \"input\": \"${param1}\"" +
                "                  }" +
                "              ]" +
                "           }," +
                "           \"parameters\": {" +
                "               \"param1\" : { \"description\" : \"Test Long parameter\"," +
                "                              \"defaultValue\" : [ \"bad arg type\" ]," +
                "                              \"requiredArg\" : false," +
                "                              \"valueClass\": \"java.lang.Long\"" +
                "                          }" +
                "           }" +
                "       }" +
                "   ]" +
                "}";

        exception.expect(SerialisationException.class);
        JSONSerialiser.deserialise(opChainJSON.getBytes(StandardCharsets.UTF_8), OperationChain.class);
    }

    @Test
    public void shouldAddNamedOperationWithScoreCorrectly() throws OperationException, CacheOperationException {
        OperationChain opChain =
                new OperationChain("opChain", new Operation("ToArray"));
        addNamedOperation.operationArg("OperationChain", opChain);
        addNamedOperation.operationArg("Score", 2);
        addNamedOperation.operationArg("OperationName", "testOp");

        handler.doOperation(addNamedOperation, context, executor);

        final NamedOperationDetail result = mockCache.getNamedOperation("testOp", new User(), EMPTY_ADMIN_AUTH);

        assert cacheContains("testOp");
        assertEquals(addNamedOperation.get("Score"), result.getScore());
    }

    private boolean cacheContains(final String opName) {
        Iterable<NamedOperationDetail> ops = mockCache.getAllNamedOperations(context.getUser(), EMPTY_ADMIN_AUTH);
        for (final NamedOperationDetail op : ops) {
            if (op.getOperationName().equals(opName)) {
                return true;
            }
        }
        return false;

    }

    @Test
    public void shouldGetOperationsWithDefaultParameters() throws Exception {
        // Given
        final Operation addNamedOperation = new Operation("AddNamedOperation")
                .operationArg("operationChain", "{\"class\":\"uk.gov.gchq.maestro.operation.OperationChain\"," +
                        "\"id\":\"ToArrayChain\"," +
                        "\"operations\":[{\"class\": \"uk.gov.gchq.maestro.operation.Operation\", " +
                        "\"input\": [\"${testParameter}\"]," +
                        "\"id\":\"ToArray\"" +
                        "}]}")
                .operationArg("description", "Test Named Operation")
                .operationArg("operationName", "Test")
                .operationArg("readAccessRoles", USER)
                .operationArg("overwriteFlag", false)
                .operationArg("writeAccessRoles", USER)
                .operationArg("parameters", new ParameterDetail.Builder()
                        .description("test")
                        .defaultValue(1)
                        .valueClass(Integer.class)
                        .required(false)
                        .build())
                .operationArg("score", 2);
        // When
        final OperationChain operationChain = JSONSerialiser.deserialise((String) addNamedOperation.get("operationChain"), OperationChain.class); //TODO this Logic needs to be in handler.

        Collection<Operation> operations = operationChain.getOperations();

        // Then
        assertEquals(
                Collections.singletonList("ToArray"),
                operations.stream().map(o -> o.getId()).collect(Collectors.toList())
        );
        final Operation nestedOp = operations.iterator().next();
        final List<? extends Integer> input =
                Lists.newArrayList((Integer) nestedOp.get("Input"));
        assertEquals(Collections.singletonList(1), input); //TODO this logic needs to be in handler
    }

    @Test
    public void shouldGetOperationsWhenNoDefaultParameter() throws SerialisationException {
        final HashMap<String, ParameterDetail> parameters = new HashMap<>();
        parameters.put("testParameter", new ParameterDetail.Builder()
                .description("the seed")
                .valueClass(String.class)
                .required(false)
                .build());

        // Given
        final Operation addNamedOperation = new Operation("AddNamedOperation")
                .operationArg("operationChain",
                        "{\"class\":\"uk.gov.gchq.maestro.operation.OperationChain\"," +
                                "\"id\":\"ToArrayChain\"," +
                                "\"operations\":[{\"class\": \"uk.gov.gchq.maestro.operation.Operation\", " +
                                "\"input\": [4]," +
                                "\"id\":\"ToArray\"" +
                                "}]}")
                .operationArg("description", "Test Named Operation")
                .operationArg("name", "Test")
                .operationArg("overwrite", false)
                .operationArg("readAccessRoles", USER)
                .operationArg("writeAccessRoles", USER)
                .operationArg("parameter", parameters)
                .operationArg("score", 2);

        // When
        final OperationChain operationChain = JSONSerialiser.deserialise((String) addNamedOperation.get("operationChain"), OperationChain.class); //TODO this Logic needs to be in handler.

        Collection<Operation> operations = operationChain.getOperations();

        // Then
        assertEquals(
                Collections.singletonList("ToArray"),
                operations.stream().map(o -> o.getId()).collect(Collectors.toList())
        );
        final Operation nestedOp = operations.iterator().next();
        final List<? extends Integer> input =
                Lists.newArrayList((Integer) nestedOp.get("Input"));
        assertEquals(Collections.singletonList(4), input);
    }
}
