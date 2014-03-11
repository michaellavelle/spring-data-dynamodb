/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.query;

import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.Select;

public class QueryRequestMapper {

	private AmazonDynamoDB amazonDynamoDB;
	private DynamoDBMapperConfig config;

	private DynamoDBMapper dynamoDBMapper;

	public QueryRequestMapper(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig config, DynamoDBMapper dynamoDBMapper) {
		this.amazonDynamoDB = amazonDynamoDB;
		this.dynamoDBMapper = dynamoDBMapper;
		this.config = config == null ? DynamoDBMapperConfig.DEFAULT : config;
	}

	public String getOverriddenTableName(final DynamoDBEntityInformation<?, ?> entityInformation) {

		String tableName = entityInformation.getDynamoDBTableName();
		if (config.getTableNameOverride() != null) {
			if (config.getTableNameOverride().getTableName() != null) {
				tableName = config.getTableNameOverride().getTableName();
			} else {
				tableName = config.getTableNameOverride().getTableNamePrefix() + tableName;
			}
		}

		return tableName;
	}

	public <T> PaginatedQueryList<T> query(Class<T> clazz, QueryRequest queryRequest) {

		QueryResult queryResult = amazonDynamoDB.query(queryRequest);
		return new PaginatedQueryList<T>(dynamoDBMapper, clazz, amazonDynamoDB, queryRequest, queryResult,
				config.getPaginationLoadingStrategy(), config);
	}
	
	public <T> long count(Class<T> clazz, QueryRequest mutableQueryRequest) {

		mutableQueryRequest.setSelect(Select.COUNT);

        // Count queries can also be truncated for large datasets
        int count = 0;
        QueryResult queryResult = null;
        do {
            queryResult = amazonDynamoDB.query(mutableQueryRequest);
            count += queryResult.getCount();
            mutableQueryRequest.setExclusiveStartKey(queryResult.getLastEvaluatedKey());
        } while (queryResult.getLastEvaluatedKey() != null);

        return count;
	}
	

}
