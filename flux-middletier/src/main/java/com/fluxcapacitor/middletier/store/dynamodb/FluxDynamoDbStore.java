/*
' * 	Copyright 2012 Chris Fregly
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
package com.fluxcapacitor.middletier.store.dynamodb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.fluxcapacitor.middletier.store.AppStore;
import com.google.common.collect.ImmutableMap;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StatsTimer;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.stats.StatsConfig;

public class FluxDynamoDbStore implements AppStore {
    private static final Logger log = LoggerFactory.getLogger(FluxDynamoDbStore.class);

    //Property names
    private static final String tablePropertyName = "logs.dynamodb.tableName";
    private static final String hashKeyAttributePropertyName = "logs.dynamodb.hashKeyName";
    private static final String rangeKeyAttributePropertyName = "logs.dynamodb.rangeKeyName";
    private static final String valueAttributePropertyName = "logs.dynamodb.valueName";

    //Property defaults
    private static final String defaultTable = "logs";
    private static final String defaultHashKeyName = "key";
    private static final String defaultRangeKeyName = "datetime";
    private static final String defaultValueName = "log";

    //Dynamic Properties
    private DynamicStringProperty tableName = DynamicPropertyFactory.getInstance()
            .getStringProperty(tablePropertyName, defaultTable);
    private DynamicStringProperty hashKeyName = DynamicPropertyFactory.getInstance()
            .getStringProperty(hashKeyAttributePropertyName, defaultHashKeyName);
    private DynamicStringProperty rangeKeyName = DynamicPropertyFactory.getInstance()
            .getStringProperty(rangeKeyAttributePropertyName, defaultRangeKeyName);
    private DynamicStringProperty valueName = DynamicPropertyFactory.getInstance()
            .getStringProperty(valueAttributePropertyName, defaultValueName);

    private AmazonDynamoDB dbClient;

    // Servo Metrics
    private static StatsTimer getLogsTimer = new StatsTimer(MonitorConfig.builder("FluxDynamoDB_GetLogs_statsTimer").build(), new StatsConfig.Builder().build());
    private static StatsTimer addLogTimer = new StatsTimer(MonitorConfig.builder("FluxDynamoDB_AddLog_statsTimer").build(), new StatsConfig.Builder().build());

    static {
    	DefaultMonitorRegistry.getInstance().register(getLogsTimer);
    	DefaultMonitorRegistry.getInstance().register(addLogTimer);
    }

    public void start() {
    	dbClient = new AmazonDynamoDBClient();
    }

	@Override
	public List<String> getLogs(String key) throws Exception {
        QueryRequest queryRequest = new QueryRequest()
                .withTableName(tableName.get())
                .withHashKeyValue(new AttributeValue(key));
        
        Stopwatch stopwatch = getLogsTimer.start();
        
        QueryResult result;
        try {
            // perform query
        	result = dbClient.query(queryRequest);
        } finally {
            getLogsTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
        
        List<String> logs = new ArrayList<String>(result.getCount());

        // prepend the total row count to the list
        logs.add(String.valueOf(result.getCount()));
        
        for (Map<String, AttributeValue> item : result.getItems()) {
        	logs.add(item.get(hashKeyName.get()).getS());
        	logs.add(item.get(rangeKeyName.get()).getS());
        	logs.add(item.get(valueName.get()).getS());
        }

        return logs;
	}

	@Override
	public long addLog(String key, String log) throws Exception {
		long dateTime = new Date().getTime();
		
		String dateTimeStr = String.valueOf(dateTime);
		
        PutItemRequest putItemRequest = new PutItemRequest()
        	.withTableName(tableName.get())
        	.withItem(ImmutableMap.of(hashKeyName.get(), new AttributeValue(key),
        							  rangeKeyName.get(), new AttributeValue(dateTimeStr),
        							  valueName.get(), new AttributeValue(log)));
        
        Stopwatch stopwatch = addLogTimer.start();

        try {
        	dbClient.putItem(putItemRequest);
        } finally {
        	addLogTimer.record(stopwatch.getDuration(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
        
        return dateTime;        
	}
}
