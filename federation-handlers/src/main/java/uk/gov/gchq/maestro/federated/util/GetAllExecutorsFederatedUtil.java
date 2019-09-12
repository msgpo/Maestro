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

package uk.gov.gchq.maestro.federated.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.maestro.commonutil.exception.MaestroCheckedException;
import uk.gov.gchq.maestro.commonutil.exception.MaestroRuntimeException;
import uk.gov.gchq.maestro.executor.Executor;
import uk.gov.gchq.maestro.executor.util.Config;
import uk.gov.gchq.maestro.federated.FederatedExecutorStorage;
import uk.gov.gchq.maestro.operation.user.User;

import java.util.Collection;

import static java.util.Objects.nonNull;

public final class GetAllExecutorsFederatedUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);
    public static final String ERROR_GETTING_S_FROM_S_S = "Error getting: %s from: %s -> %s";
    public static final String FROM_A_NULL = "Can't get Executors with a null %s";

    private GetAllExecutorsFederatedUtil() {
        //No instance
    }

    public static Collection<Executor> getAllExecutorsFrom(final Executor executor, final User user) throws MaestroCheckedException {
        if (nonNull(executor)) {
            final Config config = executor.getConfig();
            return getAllExecutorsFrom(config, user);
        } else {
            throw new MaestroRuntimeException(String.format(FROM_A_NULL, "Executor"));
        }
    }

    public static Collection<Executor> getAllExecutorsFrom(final Config config, final User user) throws MaestroCheckedException {
        if (nonNull(config)) {
            try {
                final FederatedExecutorStorage executorStorage = ExecutorStorageFederatedUtil.getExecutorStorage(config.getProperties());
                return getAllExecutorsFrom(user, executorStorage);
            } catch (final Exception e) {
                throw new MaestroCheckedException(String.format(ERROR_GETTING_S_FROM_S_S, config.getId(), config.getId(), e.getMessage()), e);
            }
        } else {
            throw new MaestroRuntimeException(String.format(FROM_A_NULL, "config"));
        }
    }

    public static Collection<Executor> getAllExecutorsFrom(final User user, final FederatedExecutorStorage executorStorage) throws MaestroCheckedException {
        if (nonNull(executorStorage)) {
            return executorStorage.getAll(user);
        } else {
            throw new MaestroRuntimeException(String.format(FROM_A_NULL, "FederatedExecutorStorage"));
        }
    }
}
