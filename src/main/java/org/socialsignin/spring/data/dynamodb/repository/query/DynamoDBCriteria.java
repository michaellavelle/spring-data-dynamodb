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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBHashAndRangeKey;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityWithCompositeIdInformation;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
/**
 * @author Michael Lavelle
 */
public class DynamoDBCriteria<T, ID extends Serializable> {

	private Object hashKeyEquals;
	private Object rangeKeyEquals;
	private String hashKeyAttributeName;
	private String hashKeyPropertyName;
	private String rangeKeyPropertyName;
	private String rangeKeyAttributeName;
	private Map<String, Condition> attributeConditions;
	private boolean compositeId;
	private DynamoDBEntityInformation<T, ID> entityMetadata;

	public DynamoDBCriteria(DynamoDBEntityInformation<T, ID> entityMetadata) {
		compositeId = entityMetadata.hasCompositeId();
		this.entityMetadata = entityMetadata;
		this.attributeConditions = new HashMap<String, Condition>();
		if (compositeId)
		{
			setupCompositeIdPropertyAttributes((DynamoDBEntityWithCompositeIdInformation<T, ID>) entityMetadata);
		}
		
	}

	private boolean isComparisonOperatorDistributive(ComparisonOperator comparisonOperator) {
		return comparisonOperator.equals(ComparisonOperator.NE) || comparisonOperator.equals(ComparisonOperator.EQ);
	}

	private String getDynamoDBAttributeName(String propertyName) {
		String overriddenName = entityMetadata.getOverriddenAttributeName(propertyName);
		return overriddenName != null ? overriddenName : propertyName;
	}
	
	private void setupCompositeIdPropertyAttributes(DynamoDBEntityWithCompositeIdInformation<T, ID> compositeMetadata)
	{
		this.hashKeyPropertyName = compositeMetadata.getHashKeyPropertyName();
		this.hashKeyAttributeName = getDynamoDBAttributeName(hashKeyPropertyName);
		this.rangeKeyPropertyName = compositeMetadata.getRangeKeyPropertyName();
		this.rangeKeyAttributeName = getDynamoDBAttributeName(rangeKeyPropertyName);

	}
	
	private void setupHashKeyPropertyAttributes(String hashKeyPropertyName)
	{
		this.hashKeyPropertyName = hashKeyPropertyName;
		this.hashKeyAttributeName = getDynamoDBAttributeName(hashKeyPropertyName);
		
	}
	
	private void setupRangeKeyPropertyAttributes(String rangeKeyPropertyName)
	{
		this.rangeKeyPropertyName = rangeKeyPropertyName;
		this.rangeKeyAttributeName = getDynamoDBAttributeName(rangeKeyPropertyName);
		
	}

	@SuppressWarnings("unchecked")
	public DynamoDBCriteria<T, ID> withPropertyCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value) {
		if (comparisonOperator.equals(ComparisonOperator.EQ)) {
			return withPropertyEquals(propertyName, value);
		} else {
			
			if (!isComparisonOperatorDistributive(comparisonOperator)) {
				throw new UnsupportedOperationException("Only EQ,NE so far supported for composite id comparison");
			}
			
			if (entityMetadata.isCompositeIdProperty(propertyName)) {
			
				Object hashKeyValue = entityMetadata.getHashKey((ID) value);
				Object rangeKeyValue = entityMetadata.getRangeKey((ID) value);
				
				Condition hashKeyCondition = createCondition(comparisonOperator, hashKeyValue, entityMetadata.getMarshallerForProperty(hashKeyPropertyName));
				Condition rangeKeyCondition = createCondition(comparisonOperator, rangeKeyValue, entityMetadata.getMarshallerForProperty(rangeKeyPropertyName));
				
				attributeConditions.put(getDynamoDBAttributeName(hashKeyPropertyName), hashKeyCondition);
				attributeConditions.put(getDynamoDBAttributeName(rangeKeyPropertyName), rangeKeyCondition);

			} else {

				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(propertyName);

				Condition condition = createCondition(comparisonOperator, value, marshaller);
				attributeConditions.put(getDynamoDBAttributeName(propertyName), condition);
			}

		}

		return this;
	}

	@SuppressWarnings("unchecked")
	public DynamoDBCriteria<T, ID> withPropertyEquals(String propertyName, Object value) {
		if (entityMetadata.hasCompositeId() && entityMetadata.isCompositeIdProperty(propertyName)) {

			this.hashKeyEquals = entityMetadata.getHashKey((ID) value);

			this.rangeKeyEquals = entityMetadata.getRangeKey((ID) value);

		}

		else if (entityMetadata.isHashKeyProperty(propertyName)) {
			this.hashKeyEquals = value;
			setupHashKeyPropertyAttributes(propertyName);

		} else if (entityMetadata.isRangeKeyProperty(propertyName)) {
			this.rangeKeyEquals = value;
			setupRangeKeyPropertyAttributes(propertyName);


		} else {

			DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(propertyName);

			Condition condition = createCondition(ComparisonOperator.EQ, value, marshaller);

			attributeConditions.put(getDynamoDBAttributeName(propertyName), condition);
		}

		return this;
	}

	private DynamoDBHashAndRangeKey createHashAndRangeEqualsCriteria() {
		DynamoDBHashAndRangeKey hashAndRangeKey = new DynamoDBHashAndRangeKey();
		hashAndRangeKey.setHashKey(hashKeyEquals);

		hashAndRangeKey.setRangeKey(rangeKeyEquals);

		return hashAndRangeKey;
	}

	private T buildHashKeyObjectFromHashKey(Object hashKey) {
		T entity = (T) entityMetadata.getHashKeyPropotypeEntityForHashKey(hashKey);
		return entity;
	}

	public DynamoDBQueryExpression<T> buildQueryExpression() {
		Map<String, Condition> rangeKeyConditions = new HashMap<String, Condition>();
		boolean allRangeKeyConditions = true;
		for (Map.Entry<String, Condition> propertyCondition : attributeConditions.entrySet()) {
			if (propertyCondition.getKey().equals(rangeKeyAttributeName)) {
				rangeKeyConditions.put(propertyCondition.getKey(), propertyCondition.getValue());
			} else {
				allRangeKeyConditions = false;
			}
		}

		if (!allRangeKeyConditions) {
			// Can only build query expression if any conditions specified are range conditions
			return null;
		}

		DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();

		DynamoDBHashAndRangeKey loadKey = buildLoadCriteria();
		if (loadKey == null) {
			if (hashKeyEquals != null) {
				queryExpression.withHashKeyValues(buildHashKeyObjectFromHashKey(hashKeyEquals));
			} else {
				return null;
			}

			if (rangeKeyEquals != null) {
				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(this.rangeKeyPropertyName);

				Condition rangeKeyCondition = createCondition(ComparisonOperator.EQ, rangeKeyEquals, marshaller);
				rangeKeyConditions = new HashMap<String, Condition>();
				rangeKeyConditions.put(rangeKeyAttributeName, rangeKeyCondition);
				queryExpression.setRangeKeyConditions(rangeKeyConditions);

			} else {
				queryExpression.withRangeKeyConditions(rangeKeyConditions);
		
			}
			return queryExpression;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <V> Condition createCondition(ComparisonOperator comparisonOperator, Object o,
			DynamoDBMarshaller<V> optionalMarshaller) {
		
		
		if (o instanceof String) {
			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withS((String) o));

			return condition;

		} else if (o instanceof Number) {

			Condition condition = new Condition().withComparisonOperator(comparisonOperator)
					.withAttributeValueList(new AttributeValue().withN(o.toString()));
			return condition;
		} else if (o instanceof Boolean) {
			boolean boolValue = ((Boolean) o).booleanValue();
			Condition condition = new Condition().withComparisonOperator(comparisonOperator)
			.withAttributeValueList(new AttributeValue().withN(boolValue ? "1" : "0"));
			return condition;
		} else if (optionalMarshaller != null) {
			String marshalledString = optionalMarshaller.marshall((V) o);
			Condition condition = new Condition().withComparisonOperator(comparisonOperator)

			.withAttributeValueList(new AttributeValue().withS(marshalledString));
			return condition;
		} else {
			throw new RuntimeException("Cannot create condition for type:" + o.getClass()
					+ " property conditions must be String,Number or Boolean, or have a DynamoDBMarshaller configured");
		}
	}

	public DynamoDBScanExpression buildScanExpression() {
		DynamoDBHashAndRangeKey loadKey = buildLoadCriteria();
		if (loadKey == null) {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			if (hashKeyEquals != null) {
				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(hashKeyPropertyName);

				Assert.notNull(hashKeyAttributeName,"No hash key attribute name set");
				Condition condition = createCondition(ComparisonOperator.EQ, hashKeyEquals, marshaller);
				scanExpression.addFilterCondition(hashKeyAttributeName, condition);
			}

			if (rangeKeyEquals != null) {
				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(rangeKeyPropertyName);

				Condition condition = createCondition(ComparisonOperator.EQ, rangeKeyEquals, marshaller);
				scanExpression.addFilterCondition(rangeKeyAttributeName, condition);
			}

			for (Map.Entry<String, Condition> attributeCondition : attributeConditions.entrySet()) {
				scanExpression.addFilterCondition(attributeCondition.getKey(), attributeCondition.getValue());
			}

			return scanExpression;
		}
		return null;
	}

	public DynamoDBHashAndRangeKey buildLoadCriteria() {
		DynamoDBHashAndRangeKey hashAndRangeEqualsCriteria = createHashAndRangeEqualsCriteria();
		if (compositeId) {
			return hashAndRangeEqualsCriteria.getHashKey() != null && hashAndRangeEqualsCriteria.getRangeKey() != null
					&& attributeConditions.size() == 0 ? hashAndRangeEqualsCriteria : null;
		} else {
			return hashAndRangeEqualsCriteria.getHashKey() != null && attributeConditions.size() == 0 ? hashAndRangeEqualsCriteria
					: null;
		}

	}

}
