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

package uk.gov.gchq.maestro.operation;

public class OperationChainTest extends OperationTest {

    @Override
    protected String getJSONString() {
        return "{\n" +
                "  \"class\" : \"uk.gov.gchq.maestro.operation.OperationChain\",\n" +
                "  \"id\" : \"testChain\",\n" +
                "  \"operations\" : [ {\n" +
                "    \"class\" : \"uk.gov.gchq.maestro.operation.Operation\",\n" +
                "    \"id\" : \"innerOp1\",\n" +
                "    \"operationArgs\" : { }\n" +
                "  }, {\n" +
                "    \"class\" : \"uk.gov.gchq.maestro.operation.Operation\",\n" +
                "    \"id\" : \"innerOp2\",\n" +
                "    \"operationArgs\" : { }\n" +
                "  } ],\n" +
                "  \"operationArgs\" : { }\n" +
                "}";
    }

    @Override
    protected Operation getFullyPopulatedTestObject() throws Exception {
        return new OperationChain("testChain", new Operation("innerOp1"), new Operation("innerOp2"));
    }
}