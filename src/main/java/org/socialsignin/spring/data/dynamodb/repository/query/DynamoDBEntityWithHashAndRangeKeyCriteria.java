package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBHashAndRangeKey;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBHashAndRangeKeyExtractingEntityInformation;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

public class DynamoDBEntityWithHashAndRangeKeyCriteria<T,ID extends Serializable> extends DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> {

	
	private DynamoDBHashAndRangeKeyExtractingEntityInformation<T,ID> dynamoDBEntityInformation;
	
	public DynamoDBEntityWithHashAndRangeKeyCriteria(DynamoDBHashAndRangeKeyExtractingEntityInformation<T,ID> dynamoDBEntityInformation) {
		super(dynamoDBEntityInformation);
		this.dynamoDBEntityInformation = dynamoDBEntityInformation;
	}
	
	private boolean isComparisonOperatorDistributive(ComparisonOperator comparisonOperator) {
		return comparisonOperator.equals(ComparisonOperator.NE) || comparisonOperator.equals(ComparisonOperator.EQ);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> withSingleValueCriteria(String propertyName,
			ComparisonOperator comparisonOperator, Object value) {
		if (comparisonOperator.equals(ComparisonOperator.EQ)) {
			return withPropertyEquals(propertyName, value);
		} else if (dynamoDBEntityInformation.isCompositeHashAndRangeKeyProperty(propertyName)) {

				if (!isComparisonOperatorDistributive(comparisonOperator)) {
					throw new UnsupportedOperationException("Only EQ,NE so far supported for composite id comparison");
				}
				
				
				Object hashKeyValue = dynamoDBEntityInformation.getHashKey((ID) value);
				Object rangeKeyValue = dynamoDBEntityInformation.getRangeKey((ID) value);

				Condition hashKeyCondition = createSingleValueCondition(dynamoDBEntityInformation.getHashKeyPropertyName(),comparisonOperator, hashKeyValue,
						dynamoDBEntityInformation.getMarshallerForProperty(dynamoDBEntityInformation.getHashKeyPropertyName()));
				Condition rangeKeyCondition = createSingleValueCondition(dynamoDBEntityInformation.getRangeKeyPropertyName(),comparisonOperator, rangeKeyValue,
						dynamoDBEntityInformation.getMarshallerForProperty(dynamoDBEntityInformation.getRangeKeyPropertyName()));

				if (hashKeyValue != null)
				{
					attributeConditions.add(getDynamoDBAttributeName(dynamoDBEntityInformation.getHashKeyPropertyName()), hashKeyCondition);
				}
				if (rangeKeyValue != null)
				{
					attributeConditions.add(getDynamoDBAttributeName(dynamoDBEntityInformation.getRangeKeyPropertyName()), rangeKeyCondition);
				}
				return this;
			} 
		else
		{
			return super.withSingleValueCriteria(propertyName, comparisonOperator, value);
		}
	}

	@Override
	public DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> withNoValuedCriteria(String propertyName, ComparisonOperator comparisonOperator) {
	
		if (dynamoDBEntityInformation.isCompositeHashAndRangeKeyProperty(propertyName)) {

			if (!isComparisonOperatorDistributive(comparisonOperator)) {
				throw new UnsupportedOperationException("Only EQ,NE so far supported for composite id comparison");
			}
			
			

			Condition hashKeyCondition = createNoValueCondition(dynamoDBEntityInformation.getHashKeyPropertyName(),comparisonOperator);
			Condition rangeKeyCondition = createNoValueCondition(dynamoDBEntityInformation.getRangeKeyPropertyName(),comparisonOperator);



			attributeConditions.add(getDynamoDBAttributeName(dynamoDBEntityInformation.getHashKeyPropertyName()), hashKeyCondition);

			attributeConditions.add(getDynamoDBAttributeName(dynamoDBEntityInformation.getRangeKeyPropertyName()), rangeKeyCondition);

			return this;
		}
		else
		{
			return super.withNoValuedCriteria(propertyName, comparisonOperator);
		}
	}



	@Override
	@SuppressWarnings("unchecked")
	public DynamoDBEntityWithHashKeyOnlyCriteria<T, ID> withPropertyEquals(String propertyName, Object value) {
		
		if (dynamoDBEntityInformation.isRangeKeyAware() && dynamoDBEntityInformation.isCompositeHashAndRangeKeyProperty(propertyName)) {

			Object hashKey = dynamoDBEntityInformation.getHashKey((ID)value);
			Object rangeKey = dynamoDBEntityInformation.getRangeKey((ID)value);
			if (hashKey != null)
			{
				withPropertyEquals(dynamoDBEntityInformation.getHashKeyPropertyName(),hashKey);
			}
			if (rangeKey != null)
			{
				withPropertyEquals(dynamoDBEntityInformation.getRangeKeyPropertyName(),rangeKey);
			}
			return this;
		}
		else
		{
			return super.withPropertyEquals(propertyName, value);
		}
		
	}



	public DynamoDBHashAndRangeKey buildLoadCriteria() {

		DynamoDBHashAndRangeKey hashAndRangeKey = new DynamoDBHashAndRangeKey();

		Object hashKeyValue  = 
				attributeConditions.size() == 0 &&
						propertyEqualValues.containsKey(dynamoDBEntityInformation.getHashKeyPropertyName())
				? propertyEqualValues.get(dynamoDBEntityInformation.getHashKeyPropertyName()) : null;
				

		
		Object rangeKeyValue  = 
						attributeConditions.size() == 0 &&
								propertyEqualValues.containsKey(dynamoDBEntityInformation.getRangeKeyPropertyName())
						? propertyEqualValues.get(dynamoDBEntityInformation.getRangeKeyPropertyName()) : null;
						
		if (hashKeyValue != null && rangeKeyValue !=null && propertyEqualValues.size() == 2)
		{
			hashAndRangeKey.setHashKey(hashKeyValue);
			hashAndRangeKey.setRangeKey(rangeKeyValue);
			return hashAndRangeKey;
		}
		else
		{
			return null;
		}
						
	}
	
	private Set<String> getRangeOrIndexRangeKeyAttributeNames()
	{
		Set<String> attributeNames = new HashSet<String>();
		if (dynamoDBEntityInformation.getIndexRangeKeyPropertyNames() == null) return null;
		for (String propertyName :dynamoDBEntityInformation.getIndexRangeKeyPropertyNames())
		{
			attributeNames.add(getDynamoDBAttributeName(propertyName));
		}

		attributeNames.add(getDynamoDBAttributeName(dynamoDBEntityInformation.getRangeKeyPropertyName()));

		return attributeNames;
	}
	
	private Set<String> getRangeOrIndexRangeKeyPropertyNames()
	{
		Set<String> attributeNames = new HashSet<String>();
		if (dynamoDBEntityInformation.getIndexRangeKeyPropertyNames() == null) return null;
		for (String propertyName :dynamoDBEntityInformation.getIndexRangeKeyPropertyNames())
		{
			attributeNames.add(propertyName);
		}

		attributeNames.add(dynamoDBEntityInformation.getRangeKeyPropertyName());

		return attributeNames;
	}
	
	public DynamoDBQueryExpression<T> buildQueryExpression() {

		
		
		
		DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();
		DynamoDBHashAndRangeKey loadKey = buildLoadCriteria();
		String rangeKeyPropertyName = dynamoDBEntityInformation.getRangeKeyPropertyName();
		String rangeKeyAttributeName = getDynamoDBAttributeName(rangeKeyPropertyName);
		if (loadKey == null) {
			
			
			// Can only build query expression if we have conditions on at most one non-hash-key property
			if (attributeConditions.size() > 1)
			{
				return null;
			}
			else if (attributeConditions.size() > 0 && propertyEqualValues.containsKey(rangeKeyPropertyName) && !attributeConditions.containsKey(rangeKeyAttributeName))
			{
				// Cannot query by range key equals and another property condition - must use scan instead

				return null;
			}
			// Can only build query expression if we have equal conditions on at least the hash key
			if (propertyEqualValues.size() == 0)
			{
				return null;
			}
			if (propertyEqualValues.size() == 1 && !propertyEqualValues.containsKey(dynamoDBEntityInformation.getHashKeyPropertyName()))
			{

				return null;
			}
			if (propertyEqualValues.size() == 2)
			{
				Set<String> allowedEqualsPropertyNames = new HashSet<String>();
				allowedEqualsPropertyNames.add(dynamoDBEntityInformation.getHashKeyPropertyName());
				allowedEqualsPropertyNames.add(dynamoDBEntityInformation.getRangeKeyPropertyName());
				if (dynamoDBEntityInformation.getIndexRangeKeyPropertyNames() != null)
				{
					allowedEqualsPropertyNames.addAll(dynamoDBEntityInformation.getIndexRangeKeyPropertyNames());
				}
				if (!propertyEqualValues.containsKey(dynamoDBEntityInformation.getHashKeyPropertyName())) return null;
				for (String equalsPropertyName : propertyEqualValues.keySet())
				{
					if (!allowedEqualsPropertyNames.contains(equalsPropertyName)) return null;
				}

				
			}
			if (propertyEqualValues.size() > 2)
			{

				return null;
			}
			
			
		
			if (attributeConditions.size() > 0)
			{
				Map.Entry<String, List<Condition>> attributeConditionsMap = attributeConditions.entrySet().iterator().next();
				if (!attributeConditionsMap.getKey().equals(rangeKeyAttributeName) && (getRangeOrIndexRangeKeyAttributeNames() == null || !getRangeOrIndexRangeKeyAttributeNames().contains(attributeConditionsMap.getKey())))
				{
					// Can only build query expression if the condition is on a range key or an index range key
					return null;
				}
			}

			Object hashKeyEqualsValue  =  (attributeConditions.size() == 0 || getRangeOrIndexRangeKeyAttributeNames().contains(attributeConditions.keySet().iterator().next())) 
					&&
							propertyEqualValues.containsKey(dynamoDBEntityInformation.getHashKeyPropertyName())
					? propertyEqualValues.get(dynamoDBEntityInformation.getHashKeyPropertyName()) : null;
			
			

			
			if (hashKeyEqualsValue != null) {
				queryExpression.withHashKeyValues(dynamoDBEntityInformation.getHashKeyPropotypeEntityForHashKey(hashKeyEqualsValue));
				queryExpression.withRangeKeyConditions(new HashMap<String,Condition>());
			} else {
				return null;
			}
			
			
			
			String rangeOrIndexRangeKeyPropertyName = null;
			for (String propertyEqualName : propertyEqualValues.keySet())
			{
				if (getRangeOrIndexRangeKeyPropertyNames().contains(propertyEqualName))
				{
					rangeOrIndexRangeKeyPropertyName = propertyEqualName;
				}
			}

			if (rangeOrIndexRangeKeyPropertyName != null) {
				Object rangeKey = propertyEqualValues.get(rangeOrIndexRangeKeyPropertyName);
				String rangeOrIndexRangeAttributeName = getDynamoDBAttributeName(rangeOrIndexRangeKeyPropertyName);
				Condition rangeKeyCondition = createSingleValueCondition(rangeOrIndexRangeKeyPropertyName,ComparisonOperator.EQ, rangeKey, null);
				Map<String,Condition> rangeKeyConditions = new HashMap<String, Condition>();
				rangeKeyConditions.put(rangeOrIndexRangeAttributeName, rangeKeyCondition);
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
						if (!order.getProperty().equals(rangeKeyPropertyName) && (dynamoDBEntityInformation.getIndexRangeKeyPropertyNames() == null || !dynamoDBEntityInformation.getIndexRangeKeyPropertyNames().contains(order.getProperty()))) {
							throw new UnsupportedOperationException("Sorting only possible on range key or index range key properties");
						} else {
							if (propertyEqualValues.containsKey(rangeKeyPropertyName) && (order.getProperty().equals(rangeKeyPropertyName)) || ((!propertyEqualValues.containsKey(rangeKeyPropertyName) && order.getProperty().equals(rangeKeyPropertyName) && queryExpression.getRangeKeyConditions().size() == 0) || queryExpression.getRangeKeyConditions().containsKey(getDynamoDBAttributeName(order.getProperty()))))
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
	
	

}
