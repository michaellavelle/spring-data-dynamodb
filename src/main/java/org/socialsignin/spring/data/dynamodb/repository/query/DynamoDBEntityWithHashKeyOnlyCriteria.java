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
import java.util.List;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.mapping.DefaultDynamoDBDateMarshaller;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBHashAndRangeKey;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Sort;
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
public class DynamoDBEntityWithHashKeyOnlyCriteria<T, ID extends Serializable> implements DynamoDBCriteria<T,ID> {

	protected Sort sort = null;
	protected MultiValueMap<String, Condition> attributeConditions;
	protected Map<String,Object> propertyEqualValues;
	
	private DynamoDBEntityInformation<T,ID> dynamoDBEntityInformation;
	
	public DynamoDBEntityWithHashKeyOnlyCriteria(DynamoDBEntityInformation<T,ID> dynamoDBEntityInformation) {
		this.dynamoDBEntityInformation = dynamoDBEntityInformation;
		this.attributeConditions = new LinkedMultiValueMap<String, Condition>();
		this.propertyEqualValues = new HashMap<String,Object>();
	}

	
	protected String getDynamoDBAttributeName(String propertyName) {
		String overriddenName = dynamoDBEntityInformation.getOverriddenAttributeName(propertyName);
		return overriddenName != null ? overriddenName : propertyName;
	}

	

	public DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> withSort(Sort sort) {
		if (this.sort == null) {
			this.sort = sort;
		} else {
			this.sort.and(sort);
		}
		return this;
	}

	public DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value) {
		if (comparisonOperator.equals(ComparisonOperator.EQ)) {
			return withPropertyEquals(propertyName, value);
		} else {	

				DynamoDBMarshaller<?> marshaller = dynamoDBEntityInformation.getMarshallerForProperty(propertyName);

				Condition condition = createSingleValueCondition(propertyName,comparisonOperator, value, marshaller);
				attributeConditions.add(getDynamoDBAttributeName(propertyName), condition);


		}

		return this;
	}
	
	public DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> withNoValuedCriteria(String propertyName, ComparisonOperator comparisonOperator) {
		
	
		Condition condition = createNoValueCondition(propertyName,comparisonOperator);
		attributeConditions.add(getDynamoDBAttributeName(propertyName), condition);
		

		return this;
	}

	public DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> withPropertyEquals(String propertyName, Object value) {
	
		DynamoDBMarshaller<?> marshaller = dynamoDBEntityInformation.getMarshallerForProperty(propertyName);
		Object attributeValue = marshaller == null ? value : getMarshalledValue(marshaller,value);
		propertyEqualValues.put(propertyName,attributeValue);
		
		return this;
	}


	
	@SuppressWarnings("unchecked")
	private <V> String getMarshalledValue(DynamoDBMarshaller<V> marshaller,Object o)
	{
		return marshaller.marshall((V)o);
	}

	protected Condition createSingleValueCondition(String propertyName,ComparisonOperator comparisonOperator, Object o,
			DynamoDBMarshaller<?> optionalMarshaller) {
				
		Assert.notNull(o,"Creating conditions on null property values not yet supported: please specify a value for '" + propertyName + "'");
		
		
		if (optionalMarshaller != null) {
			String marshalledString = getMarshalledValue(optionalMarshaller,o);
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
	
	protected <V> Condition createNoValueCondition(String propertyName,ComparisonOperator comparisonOperator) {
				
		Condition condition = new Condition().withComparisonOperator(comparisonOperator);

		return condition;
	}
	
	public DynamoDBQueryExpression<T> buildQueryExpression()
	{
		return null;
	}

	public DynamoDBScanExpression buildScanExpression() {

		DynamoDBHashAndRangeKey loadKey = buildLoadCriteria();
		if (loadKey == null) {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
			for (Map.Entry<String, Object> propertyEqualEntry : propertyEqualValues.entrySet())
			{
				Condition condition = createSingleValueCondition(propertyEqualEntry.getKey(),ComparisonOperator.EQ, propertyEqualEntry.getValue(), null);
				scanExpression.addFilterCondition(getDynamoDBAttributeName(propertyEqualEntry.getKey()), condition);
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

		Object hashKeyValue  = propertyEqualValues.size() == 1 && 
				attributeConditions.size() == 0 &&
						propertyEqualValues.containsKey(dynamoDBEntityInformation.getHashKeyPropertyName())
				? propertyEqualValues.get(dynamoDBEntityInformation.getHashKeyPropertyName()) : null;
		

		if (hashKeyValue == null) return null;
		
		DynamoDBHashAndRangeKey hashAndRangeKey = new DynamoDBHashAndRangeKey();
		hashAndRangeKey.setHashKey(hashKeyValue);

		return hashAndRangeKey;
	}

}
