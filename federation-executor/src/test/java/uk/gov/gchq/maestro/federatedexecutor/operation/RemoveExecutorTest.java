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

package uk.gov.gchq.maestro.federatedexecutor.operation;

import uk.gov.gchq.maestro.helper.MaestroObjectTest;

public class RemoveExecutorTest extends MaestroObjectTest<RemoveExecutor> {
    @Override
    protected Class<RemoveExecutor> getTestObjectClass() {
        return RemoveExecutor.class;
    }

    @Override
    protected String getJSONString() {
        return "{\n" +
                "  \"class\" : \"uk.gov.gchq.maestro.federatedexecutor.operation.RemoveExecutor\",\n" +
                "  \"graphId\" : \"innerGraph1\",\n" +
                "  \"options\" : {\n" +
                "    \"maestro.federated.operation.executorIds\" : \"\"\n" +
                "  }\n" +
                "}";
    }

    @Override
    protected RemoveExecutor getTestObject() {
        return new RemoveExecutor().graphId("innerGraph1");
    }
}