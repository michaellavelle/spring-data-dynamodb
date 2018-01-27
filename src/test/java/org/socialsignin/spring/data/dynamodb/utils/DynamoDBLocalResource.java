/**
 * Copyright © 2013 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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
package org.socialsignin.spring.data.dynamodb.utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.junit.rules.ExternalResource;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityMetadataSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DynamoDBLocalResource extends ExternalResource {

    private AmazonDynamoDB ddb;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        ddb = DynamoDBEmbedded.create().amazonDynamoDB();
        return ddb;
    }

    public CreateTableResult createTable(Class<?> domainType) {
        DynamoDBEntityMetadataSupport support = new DynamoDBEntityMetadataSupport(domainType);

        String tableName = support.getDynamoDBTableName();
        String hashKey = support.getHashKeyPropertyName();
        String rangeKey = support.getHashKeyPropertyName();

        return createTable(tableName, hashKey, rangeKey);
    }

    private CreateTableResult createTable(String tableName, String hashKeyName, String rangeKeyName) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition(hashKeyName, ScalarAttributeType.S));

        List<KeySchemaElement> ks = new ArrayList<>();
        ks.add(new KeySchemaElement(hashKeyName, KeyType.HASH));

        if (rangeKeyName != null) {
            attributeDefinitions.add(new AttributeDefinition(rangeKeyName, ScalarAttributeType.S));

            ks.add(new KeySchemaElement(rangeKeyName, KeyType.RANGE));
        }

        ProvisionedThroughput provisionedthroughput = new ProvisionedThroughput(10L, 10L);

        CreateTableRequest request =
                new CreateTableRequest()
                        .withTableName(tableName)
                        .withAttributeDefinitions(attributeDefinitions)
                        .withKeySchema(ks)
                        .withProvisionedThroughput(provisionedthroughput);

        return ddb.createTable(request);
    }

    @Override
    protected void after() {
        ddb.shutdown();
    };
}
