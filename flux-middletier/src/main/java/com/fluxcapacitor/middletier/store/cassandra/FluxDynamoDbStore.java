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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class FluxDynamoDbStore implements AppStore {
    private static final Logger log = LoggerFactory.getLogger(FluxDynamoDbStore.class);

    //Property names
    private static final String tablePropertyName = "logs.dynamodb.tableName";
    private static final String keyAttributePropertyName = "logs.dynamodb.keyName";
    private static final String valueAttributePropertyName = "logs.dynamodb.valueName";

    //Property defaults
    private static final String defaultTable = "logs";
    private static final String defaultKeyName = "key";
    private static final String defaultValueName = "log";

    //Dynamic Properties
    private DynamicStringProperty tableName = DynamicPropertyFactory.getInstance()
            .getStringProperty(tablePropertyName, defaultTable);
    private DynamicStringProperty keyName = DynamicPropertyFactory.getInstance()
            .getStringProperty(keyAttributePropertyName, defaultKeyName);
    private DynamicStringProperty valueName = DynamicPropertyFactory.getInstance()
            .getStringProperty(valueAttributePropertyName, defaultValueName);

    private AmazonDynamoDB dbClient;
    
    public void start() {
    	dbClient = new AmazonDynamoDBClient();
    }

	@Override
	public List<String> getLogs(String key) throws Exception {
        QueryRequest queryRequest = new QueryRequest()
                .withTableName(tableName.get())
                .withHashKeyValue(new AttributeValue(key));
        
        QueryResult result = dbClient.query(queryRequest);
        
        List<String> logs = new ArrayList<String>(result.getCount());

        for (Map<String, AttributeValue> item : result.getItems()) {
            logs.add(item.get(valueName.get()).getS());
        }

        return logs;
	}

	@Override
	public long addLog(String key, String log) throws Exception {
        PutItemRequest putItemRequest = new PutItemRequest()
        	.withTableName(tableName.get())
        	.withItem(ImmutableMap.of(keyName.get(), new AttributeValue(key), valueName.get(), new AttributeValue(log)));

        // TODO:  add timestamp similar to cassandra 

        dbClient.putItem(putItemRequest);
        
        // TODO:  return timestamp similar to cassandra
        return 0;        
	}
}
