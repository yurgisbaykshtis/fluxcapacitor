/*
 * 	Copyright 2012 Chris Fregly
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.fluxcapacitor.middletier.store.cassandra;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluxcapacitor.core.FluxConstants;
import com.fluxcapacitor.middletier.store.AppStore;
import com.google.common.collect.ImmutableList;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.NotFoundException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.Slf4jConnectionPoolMonitorImpl;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolType;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import com.netflix.governator.annotations.AutoBindSingleton;
import com.netflix.karyon.spi.Component;
import com.netflix.config.DynamicPropertyFactory;

// Uncomment out these annotation if using Cassandra
@Component
@AutoBindSingleton(AppStore.class)
public class FluxCassandraStore implements AppStore, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(FluxCassandraStore.class);

    private Keyspace fluxKeyspace;
    private ColumnFamily<String, String> logsCF;

    public FluxCassandraStore() {
//        fluxKeyspace = createKeyspace();
        logsCF = createLogsColumnFamily();
    }

    synchronized private void ensureKeySpace() {
        if (fluxKeyspace == null) {
            fluxKeyspace = createKeyspace();
        }
    }
    
    @Override
    public List<String> getLogs(String key) throws Exception {
        OperationResult<ColumnList<String>> response;
        try {
            ensureKeySpace();
            response = fluxKeyspace.prepareQuery(logsCF).getKey(key).execute();
        } catch (NotFoundException exc) {
            logger.error("No records found for this key: " + key);
            throw exc;
        } catch (Exception exc2) {
            logger.error("Exception occurred when fetching from Cassandra for key {}: {}", key, exc2);
            throw exc2;
        }

        final List<String> items = new ArrayList<String>();
        if (response != null) {
            final ColumnList<String> columns = response.getResult();
            for (Column<String> column : columns) {
                items.add(column.getStringValue());
            }
        }

        return ImmutableList.copyOf(items);
    }

    @Override
    public long addLog(String key, String log) throws Exception {
        try {
            ensureKeySpace();
            long timestamp = fluxKeyspace.getConfig().getClock().getCurrentTime();

            OperationResult<Void> opr = fluxKeyspace.prepareColumnMutation(logsCF, key, String.valueOf(timestamp))
                    .putValue(log, null).execute();

            logger.info("Time taken to add to Cassandra (in ms): " + opr.getLatency(TimeUnit.MILLISECONDS));

            return timestamp;
        } catch (Exception e) {
            logger.error("Exception occurred when writing to Cassandra: " + e);
            throw e;
        }
    }

    @Override
    public void close() {
    }

    /**
     * Connect to Cassandra
     */
    private Keyspace createKeyspace() {
        try {
            String keyspace = DynamicPropertyFactory.getInstance().getStringProperty(FluxConstants.CASSANDRA_KEYSPACE, "not-found-in-flux-configuration").get();
            String host = DynamicPropertyFactory.getInstance().getStringProperty(FluxConstants.CASSANDRA_HOST, "not-found-in-flux-configuration").get();
            int port = DynamicPropertyFactory.getInstance().getIntProperty(FluxConstants.CASSANDRA_PORT, Integer.MIN_VALUE).get();
            int maxConns = DynamicPropertyFactory.getInstance().getIntProperty(FluxConstants.CASSANDRA_MAXCONNSPERHOST, Integer.MIN_VALUE).get();
            logger.info("Creating cassandra keyspace {} for host {}", keyspace, host + port);
            AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
                    .forKeyspace(keyspace)
                    .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                            .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                            .setConnectionPoolType(ConnectionPoolType.ROUND_ROBIN)
                    )
                    .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("FluxCassandraConnectionPool")
                            .setPort(port)
                            .setMaxConnsPerHost(maxConns)
                            .setSeeds(host + ":" + port)
                    )
                    .withConnectionPoolMonitor(new Slf4jConnectionPoolMonitorImpl())
                    .buildKeyspace(ThriftFamilyFactory.getInstance());

            context.start();

            return context.getEntity();
        } catch (Exception e) {
            logger.error("Exception occurred when initializing Cassandra keyspace: " + e);
            throw new RuntimeException(e);
        }
    }

    private ColumnFamily<String, String> createLogsColumnFamily() {
        return new ColumnFamily<String, String>(
                DynamicPropertyFactory.getInstance().getStringProperty(FluxConstants.CASSANDRA_COLUMNFAMILY, "not-found-in-flux-configuration").get(),
                StringSerializer.get(),
                StringSerializer.get());
    }
}
