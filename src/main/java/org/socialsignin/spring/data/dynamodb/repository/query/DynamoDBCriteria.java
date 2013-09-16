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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.socialsignin.spring.data.dynamodb.mapping.DefaultDynamoDBDateMarshaller;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBHashAndRangeKey;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityWithCompositeIdInformation;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
	private boolean hashKeyEqualsSpecified;
	private Object rangeKeyEquals;
	private boolean rangeKeyEqualsSpecified;

	private String hashKeyAttributeName;
	private String hashKeyPropertyName;
	private String rangeKeyPropertyName;
	private String rangeKeyAttributeName;
	private Set<String> indexRangeKeyPropertyNames;
	private Set<String> indexRangeKeyAttributeNames;
	private MultiValueMap<String, Condition> attributeConditions;
	private boolean compositeId;
	private DynamoDBEntityInformation<T, ID> entityMetadata;
	private Sort sort = null;

	public DynamoDBCriteria(DynamoDBEntityInformation<T, ID> entityMetadata) {
		compositeId = entityMetadata.hasCompositeId();
		this.entityMetadata = entityMetadata;
		this.attributeConditions = new LinkedMultiValueMap<String, Condition>();
		if (compositeId) {
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

	private void setupCompositeIdPropertyAttributes(DynamoDBEntityWithCompositeIdInformation<T, ID> compositeMetadata) {
		this.hashKeyPropertyName = compositeMetadata.getHashKeyPropertyName();
		this.hashKeyAttributeName = getDynamoDBAttributeName(hashKeyPropertyName);
		this.rangeKeyPropertyName = compositeMetadata.getRangeKeyPropertyName();
		this.rangeKeyAttributeName = getDynamoDBAttributeName(rangeKeyPropertyName);
		this.indexRangeKeyPropertyNames = compositeMetadata.getIndexRangeKeyPropertyNames();
		this.indexRangeKeyAttributeNames = new HashSet<String>();
		for (String propertyName : indexRangeKeyPropertyNames)
		{
			indexRangeKeyAttributeNames.add(getDynamoDBAttributeName(propertyName));
		}

	}

	private void setupHashKeyPropertyAttributes(String hashKeyPropertyName) {
		this.hashKeyPropertyName = hashKeyPropertyName;
		this.hashKeyAttributeName = getDynamoDBAttributeName(hashKeyPropertyName);

	}

	private void setupRangeKeyPropertyAttributes(String rangeKeyPropertyName) {
		this.rangeKeyPropertyName = rangeKeyPropertyName;
		this.rangeKeyAttributeName = getDynamoDBAttributeName(rangeKeyPropertyName);

	}

	public DynamoDBCriteria<T, ID> withSort(Sort sort) {
		if (this.sort == null) {
			this.sort = sort;
		} else {
			this.sort.and(sort);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public DynamoDBCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value) {
		if (comparisonOperator.equals(ComparisonOperator.EQ)) {
			return withPropertyEquals(propertyName, value);
		} else {	

			if (entityMetadata.isCompositeIdProperty(propertyName)) {

				if (!isComparisonOperatorDistributive(comparisonOperator)) {
					throw new UnsupportedOperationException("Only EQ,NE so far supported for composite id comparison");
				}
				
				
				Object hashKeyValue = entityMetadata.getHashKey((ID) value);
				Object rangeKeyValue = entityMetadata.getRangeKey((ID) value);

				Condition hashKeyCondition = createSingleValueCondition(hashKeyPropertyName,comparisonOperator, hashKeyValue,
						entityMetadata.getMarshallerForProperty(hashKeyPropertyName));
				Condition rangeKeyCondition = createSingleValueCondition(rangeKeyPropertyName,comparisonOperator, rangeKeyValue,
						entityMetadata.getMarshallerForProperty(rangeKeyPropertyName));

				if (hashKeyValue != null)
				{
					attributeConditions.add(getDynamoDBAttributeName(hashKeyPropertyName), hashKeyCondition);
				}
				if (rangeKeyValue != null)
				{
					attributeConditions.add(getDynamoDBAttributeName(rangeKeyPropertyName), rangeKeyCondition);
				}
			} else {

				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(propertyName);

				Condition condition = createSingleValueCondition(propertyName,comparisonOperator, value, marshaller);
				attributeConditions.add(getDynamoDBAttributeName(propertyName), condition);
			}

		}

		return this;
	}
	
	@SuppressWarnings("unchecked")
	public DynamoDBCriteria<T, ID> withNoValuedCriteria(String propertyName, ComparisonOperator comparisonOperator) {
		
			if (entityMetadata.isCompositeIdProperty(propertyName)) {

				if (!isComparisonOperatorDistributive(comparisonOperator)) {
					throw new UnsupportedOperationException("Only EQ,NE so far supported for composite id comparison");
				}
				
				

				Condition hashKeyCondition = createNoValueCondition(hashKeyPropertyName,comparisonOperator);
				Condition rangeKeyCondition = createNoValueCondition(rangeKeyPropertyName,comparisonOperator);



				attributeConditions.add(getDynamoDBAttributeName(hashKeyPropertyName), hashKeyCondition);

				attributeConditions.add(getDynamoDBAttributeName(rangeKeyPropertyName), rangeKeyCondition);

			} else {

				Condition condition = createNoValueCondition(propertyName,comparisonOperator);
				attributeConditions.add(getDynamoDBAttributeName(propertyName), condition);
			}

		

		return this;
	}

	@SuppressWarnings("unchecked")
	public DynamoDBCriteria<T, ID> withPropertyEquals(String propertyName, Object value) {
		
		if (entityMetadata.hasCompositeId() && entityMetadata.isCompositeIdProperty(propertyName)) {

			this.hashKeyEquals = entityMetadata.getHashKey((ID) value);
			this.hashKeyEqualsSpecified = hashKeyEquals != null;
			this.rangeKeyEquals = entityMetadata.getRangeKey((ID) value);
			this.rangeKeyEqualsSpecified = rangeKeyEquals != null;
		}

		else if (entityMetadata.isHashKeyProperty(propertyName)) {
			this.hashKeyEquals = value;
			this.hashKeyEqualsSpecified =true;
			setupHashKeyPropertyAttributes(propertyName);

		} else if (entityMetadata.isRangeKeyProperty(propertyName)) {
			this.rangeKeyEquals = value;
			this.rangeKeyEqualsSpecified = true;
			setupRangeKeyPropertyAttributes(propertyName);

		} else {

			DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(propertyName);

			Condition condition = createSingleValueCondition(propertyName,ComparisonOperator.EQ, value, marshaller);

			attributeConditions.add(getDynamoDBAttributeName(propertyName), condition);
		}

		return this;
	}

	private DynamoDBHashAndRangeKey createHashAndRangeEqualsCriteria() {
		DynamoDBHashAndRangeKey hashAndRangeKey = new DynamoDBHashAndRangeKey();
		hashAndRangeKey.setHashKey(hashKeyEquals);

		hashAndRangeKey.setRangeKey(rangeKeyEquals);

		return hashAndRangeKey;
	}

	private T buildHashKeyObjectFromHashKey(Object hashKey,String propertyName) {
		
		Assert.notNull(hashKey,"Querying by null hash key not supported, please provide a value for '" + propertyName + "'");
		
		T entity = (T) entityMetadata.getHashKeyPropotypeEntityForHashKey(hashKey);
		return entity;
	}

	public DynamoDBQueryExpression<T> buildQueryExpression() {

		
		
	
		DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();
		DynamoDBHashAndRangeKey loadKey = buildLoadCriteria();
		if (loadKey == null) {
			
			// Can only build query expression if we have conditions on at most one non-hash-key property
			if (attributeConditions.size() > 1)
			{
				return null;
			}
			else if (attributeConditions.size() > 0 && rangeKeyEqualsSpecified && !attributeConditions.containsKey(rangeKeyAttributeName))
			{
				// Cannot query by range key equals and another property condition - must use scan instead
				return null;
			}
			
		
			if (attributeConditions.size() > 0)
			{
				Map.Entry<String, List<Condition>> propertyConditions = attributeConditions.entrySet().iterator().next();
				if (!propertyConditions.getKey().equals(rangeKeyAttributeName) && (indexRangeKeyAttributeNames == null || !indexRangeKeyAttributeNames.contains(propertyConditions.getKey())))
				{
					// Can only build query expression if the condition is on a range key or an index range key
					return null;
				}
			}
			
			
			
			if (hashKeyEqualsSpecified) {
				queryExpression.withHashKeyValues(buildHashKeyObjectFromHashKey(hashKeyEquals,hashKeyPropertyName));
				queryExpression.withRangeKeyConditions(new HashMap<String,Condition>());
			} else {
				return null;
			}

			if (rangeKeyEqualsSpecified) {
				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(this.rangeKeyPropertyName);

				Condition rangeKeyCondition = createSingleValueCondition(rangeKeyPropertyName,ComparisonOperator.EQ, rangeKeyEquals, marshaller);
				Map<String,Condition> rangeKeyConditions = new HashMap<String, Condition>();
				rangeKeyConditions.put(rangeKeyAttributeName, rangeKeyCondition);
				queryExpression.setRangeKeyConditions(rangeKeyConditions);
				// TODO Apply other range conditions here - amazon db will not accept range conditions on more than one field
				// but how will this behave if another range condition on same field?
				// This may not be needed
				for (Entry<String, List<Condition>> entry :  attributeConditions.entrySet())
				{
					for (Condition condition : entry.getValue())
					{
						queryExpression.withRangeKeyCondition(entry.getKey(), condition);
					}
				}

			} else {
				for (Entry<String, List<Condition>> entry :  attributeConditions.entrySet())
				{
					for (Condition condition : entry.getValue())
					{
						queryExpression.withRangeKeyCondition(entry.getKey(), condition);
					}
				}

			}

			if (sort != null) {
				if (rangeKeyPropertyName == null) {
					throw new UnsupportedOperationException("Sort not supported for entities without a range key");
				} else {
					
					Order specifiedOrder = null;
					for (Order order : sort) {
						if (specifiedOrder != null && !specifiedOrder.getProperty().equals(order.getProperty()))
						{
							throw new UnsupportedOperationException("Sorting only possible by a single property");
						}
						if (!order.getProperty().equals(rangeKeyPropertyName) && (indexRangeKeyPropertyNames == null || !indexRangeKeyPropertyNames.contains(order.getProperty()))) {
							throw new UnsupportedOperationException("Sorting only possible on range key or index range key properties");
						} else {
							if (rangeKeyEqualsSpecified && (order.getProperty().equals(rangeKeyPropertyName)) || ((!rangeKeyEqualsSpecified && order.getProperty().equals(rangeKeyPropertyName) && queryExpression.getRangeKeyConditions().size() == 0) || queryExpression.getRangeKeyConditions().containsKey(getDynamoDBAttributeName(order.getProperty()))))
							{
								queryExpression.setScanIndexForward(order.getDirection().equals(Direction.ASC));
								specifiedOrder = order;
							}
							else
							{
								throw new UnsupportedOperationException("Sorting only possible if the sort property is part of the range key criteria for this query");
							}
						}
					}
					
					
					
				}
			}

			return queryExpression;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <V> Condition createSingleValueCondition(String propertyName,ComparisonOperator comparisonOperator, Object o,
			DynamoDBMarshaller<V> optionalMarshaller) {
				
		Assert.notNull(o,"Creating conditions on null property values not yet supported: please specify a value for '" + propertyName + "'");
		
		
		if (optionalMarshaller != null) {
			String marshalledString = optionalMarshaller.marshall((V) o);
			Condition condition = new Condition().withComparisonOperator(comparisonOperator)

			.withAttributeValueList(new AttributeValue().withS(marshalledString));
			return condition;
		}
		else if (o instanceof String) {
			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withS((String) o));

			return condition;

		} else if (o instanceof Number) {

			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withN(o.toString()));
			return condition;
		} else if (o instanceof Boolean) {
			boolean boolValue = ((Boolean) o).booleanValue();
			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withN(boolValue ? "1" : "0"));
			return condition;
		} else if (o instanceof Date) {
			Date date = (Date)o;
			String marshalledDate = new DefaultDynamoDBDateMarshaller().marshall(date);

			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withS(marshalledDate));
			return condition;
		} else {
			throw new RuntimeException("Cannot create condition for type:" + o.getClass()
					+ " property conditions must be String,Number or Boolean, or have a DynamoDBMarshaller configured");
		}
	}
	
	@SuppressWarnings("unchecked")
	private <V> Condition createNoValueCondition(String propertyName,ComparisonOperator comparisonOperator) {
				
		Condition condition = new Condition().withComparisonOperator(comparisonOperator);

		return condition;
	}

	public DynamoDBScanExpression buildScanExpression() {

		DynamoDBHashAndRangeKey loadKey = buildLoadCriteria();
		if (loadKey == null) {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			if (hashKeyEqualsSpecified) {
				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(hashKeyPropertyName);

				Assert.notNull(hashKeyAttributeName, "No hash key attribute name set");
				Condition condition = createSingleValueCondition(hashKeyPropertyName,ComparisonOperator.EQ, hashKeyEquals, marshaller);
				scanExpression.addFilterCondition(hashKeyAttributeName, condition);
			}
			

			if (rangeKeyEqualsSpecified) {
				DynamoDBMarshaller<?> marshaller = entityMetadata.getMarshallerForProperty(rangeKeyPropertyName);

				Condition condition = createSingleValueCondition(rangeKeyPropertyName,ComparisonOperator.EQ, rangeKeyEquals, marshaller);
				scanExpression.addFilterCondition(rangeKeyAttributeName, condition);
			}

			for (Map.Entry<String, List<Condition>> attributeCondition : attributeConditions.entrySet()) {
				for (Condition condition : attributeCondition.getValue())
				{
					scanExpression.addFilterCondition(attributeCondition.getKey(), condition);
				}
			}

			if (sort != null) {
				throw new UnsupportedOperationException("Sort not supported for scan expressions");
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
