/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.query.CountByHashKeyQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityQueryRequestQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityScanExpressionQuery;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.query.QueryRequestCountQuery;
import org.socialsignin.spring.data.dynamodb.query.QueryRequestMapper;
import org.socialsignin.spring.data.dynamodb.query.ScanExpressionCountQuery;
import org.socialsignin.spring.data.dynamodb.query.SingleEntityLoadByHashKeyQuery;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;

/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityWithHashKeyOnlyCriteria<T, ID extends Serializable> extends AbstractDynamoDBQueryCriteria<T, ID> {

	private DynamoDBEntityInformation<T, ID> entityInformation;

	public DynamoDBEntityWithHashKeyOnlyCriteria(DynamoDBEntityInformation<T, ID> entityInformation) {
		super(entityInformation);
		this.entityInformation = entityInformation;
	}

	protected Query<T> buildSingleEntityLoadQuery(DynamoDBMapper dynamoDBMapper) {
		return new SingleEntityLoadByHashKeyQuery<T>(dynamoDBMapper, clazz, getHashKeyPropertyValue());
	}
	
	protected Query<Long> buildSingleEntityCountQuery(DynamoDBMapper dynamoDBMapper) {
		return new CountByHashKeyQuery<T>(dynamoDBMapper, clazz, getHashKeyPropertyValue());
	}

	protected Query<T> buildFinderQuery(DynamoDBMapper dynamoDBMapper, QueryRequestMapper queryRequestMapper) {
		if (isApplicableForGlobalSecondaryIndex()) {

			List<Condition> hashKeyConditions = getHashKeyConditions();
			QueryRequest queryRequest = buildQueryRequest(queryRequestMapper.getOverriddenTableName(entityInformation),
					getGlobalSecondaryIndexName(), getHashKeyAttributeName(), null, null, hashKeyConditions, null);
			return new MultipleEntityQueryRequestQuery<T>(queryRequestMapper, entityInformation.getJavaType(), queryRequest);
		} else {
			return new MultipleEntityScanExpressionQuery<T>(dynamoDBMapper, clazz, buildScanExpression());
		}
	}
	
	protected Query<Long> buildFinderCountQuery(DynamoDBMapper dynamoDBMapper, QueryRequestMapper queryRequestMapper,boolean pageQuery) {
		if (isApplicableForGlobalSecondaryIndex()) {

			List<Condition> hashKeyConditions = getHashKeyConditions();
			QueryRequest queryRequest = buildQueryRequest(queryRequestMapper.getOverriddenTableName(entityInformation),
					getGlobalSecondaryIndexName(), getHashKeyAttributeName(), null, null, hashKeyConditions, null);
			return new QueryRequestCountQuery<T>(queryRequestMapper, entityInformation.getJavaType(), queryRequest);

		} else {
			return new ScanExpressionCountQuery<T>(dynamoDBMapper, clazz, buildScanExpression(),pageQuery);
		}
	}

	@Override
	protected boolean isOnlyHashKeySpecified() {
		return attributeConditions.size() == 0 && isHashKeySpecified();
	}

	@Override
	public boolean isApplicableForLoad() {
		return isOnlyHashKeySpecified();
	}

	public DynamoDBScanExpression buildScanExpression() {

		if (sort != null) {
			throw new UnsupportedOperationException("Sort not supported for scan expressions");
		}

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		if (isHashKeySpecified()) {
			scanExpression.addFilterCondition(
					getHashKeyAttributeName(),
					createSingleValueCondition(getHashKeyPropertyName(), ComparisonOperator.EQ, getHashKeyAttributeValue(),
							getHashKeyAttributeValue().getClass(), true));
		}

		for (Map.Entry<String, List<Condition>> conditionEntry : attributeConditions.entrySet()) {
			for (Condition condition : conditionEntry.getValue()) {
				scanExpression.addFilterCondition(conditionEntry.getKey(), condition);
			}
		}
		return scanExpression;
	}

	@Override
	public DynamoDBQueryCriteria<T, ID> withPropertyEquals(String propertyName, Object value, Class<?> propertyType) {
		if (isHashKeyProperty(propertyName)) {
			return withHashKeyEquals(value);
		} else {
			Condition condition = createSingleValueCondition(propertyName, ComparisonOperator.EQ, value, propertyType, false);
			return withCondition(propertyName, condition);
		}
	}

}
