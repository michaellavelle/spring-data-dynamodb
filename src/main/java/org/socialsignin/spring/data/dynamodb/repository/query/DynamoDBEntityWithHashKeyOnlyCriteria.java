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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.query.CountByHashKeyQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityQueryRequestQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityScanExpressionQuery;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.query.QueryRequestCountQuery;
import org.socialsignin.spring.data.dynamodb.query.ScanExpressionCountQuery;
import org.socialsignin.spring.data.dynamodb.query.SingleEntityLoadByHashKeyQuery;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> extends AbstractDynamoDBQueryCriteria<T, ID> {

	private DynamoDBEntityInformation<T, ID> entityInformation;

	public DynamoDBEntityWithHashKeyOnlyCriteria(DynamoDBEntityInformation<T, ID> entityInformation, DynamoDBMapperTableModel<T> tableModel) {
		super(entityInformation, tableModel);
		this.entityInformation = entityInformation;
	}

	protected Query<T> buildSingleEntityLoadQuery(DynamoDBOperations dynamoDBOperations) {
		return new SingleEntityLoadByHashKeyQuery<T>(dynamoDBOperations, clazz, getHashKeyPropertyValue());
	}
	
	protected Query<Long> buildSingleEntityCountQuery(DynamoDBOperations dynamoDBOperations) {
		return new CountByHashKeyQuery<T>(dynamoDBOperations, clazz, getHashKeyPropertyValue());
	}

	protected Query<T> buildFinderQuery(DynamoDBOperations dynamoDBOperations) {
		if (isApplicableForGlobalSecondaryIndex()) {

			List<Condition> hashKeyConditions = getHashKeyConditions();
			QueryRequest queryRequest = buildQueryRequest(dynamoDBOperations.getOverriddenTableName(clazz, entityInformation.getDynamoDBTableName()),
					getGlobalSecondaryIndexName(), getHashKeyAttributeName(), null, null, hashKeyConditions, null);
			return new MultipleEntityQueryRequestQuery<>(dynamoDBOperations,entityInformation.getJavaType(), queryRequest);
		} else {
			return new MultipleEntityScanExpressionQuery<>(dynamoDBOperations, clazz, buildScanExpression());
		}
	}
	
	protected Query<Long> buildFinderCountQuery(DynamoDBOperations dynamoDBOperations,boolean pageQuery) {
		if (isApplicableForGlobalSecondaryIndex()) {

			List<Condition> hashKeyConditions = getHashKeyConditions();
			QueryRequest queryRequest = buildQueryRequest(dynamoDBOperations.getOverriddenTableName(clazz, entityInformation.getDynamoDBTableName()),
					getGlobalSecondaryIndexName(), getHashKeyAttributeName(), null, null, hashKeyConditions, null);
			return new QueryRequestCountQuery<>(dynamoDBOperations, entityInformation.getJavaType(), queryRequest);

		} else {
			return new ScanExpressionCountQuery<>(dynamoDBOperations, clazz, buildScanExpression(),pageQuery);
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

		ensureNoSort();

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
