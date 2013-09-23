package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.socialsignin.spring.data.dynamodb.query.MultipleEntityQueryExpressionQuery;
import org.socialsignin.spring.data.dynamodb.query.MultipleEntityScanExpressionQuery;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.query.SingleEntityLoadByHashAndRangeKeyQuery;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBIdIsHashAndRangeKeyEntityInformation;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

public class DynamoDBEntityWithHashAndRangeKeyCriteria<T,ID extends Serializable> extends AbstractDynamoDBQueryCriteria<T, ID> {

	
	private Object rangeKeyAttributeValue;
	private String rangeKeyPropertyName;
	private Set<String> indexRangeKeyPropertyNames;
	private DynamoDBIdIsHashAndRangeKeyEntityInformation<T,ID> entityInformation;
	
	protected String getRangeKeyAttributeName() {
		return getAttributeName(getRangeKeyPropertyName());
	}

	protected String getRangeKeyPropertyName() {
		return rangeKeyPropertyName;
	}
	
	protected boolean isRangeKeyProperty(String propertyName)
	{
		return rangeKeyPropertyName.equals(propertyName);
	}

	public DynamoDBEntityWithHashAndRangeKeyCriteria(DynamoDBIdIsHashAndRangeKeyEntityInformation<T,ID> entityInformation) {
		super(entityInformation);
		this.rangeKeyPropertyName = entityInformation.getRangeKeyPropertyName();
		this.indexRangeKeyPropertyNames = entityInformation.getIndexRangeKeyPropertyNames();
		if (indexRangeKeyPropertyNames == null)
		{
			indexRangeKeyPropertyNames = new HashSet<String>();
		}
		this.entityInformation = entityInformation;

	}
	
	public Set<String> getIndexRangeKeyAttributeNames()
	{
		Set<String> indexRangeKeyAttributeNames = new HashSet<String>();
		for (String indexRangeKeyPropertyName :indexRangeKeyPropertyNames)
		{
			indexRangeKeyAttributeNames.add(getAttributeName(indexRangeKeyPropertyName));
		}
		return indexRangeKeyAttributeNames;
	}
	
	protected Object getRangeKeyAttributeValue()
	{
		return rangeKeyAttributeValue;
	}
	
	protected boolean isRangeKeySpecified()
	{
		return getRangeKeyAttributeValue() != null;
	}
	
	protected Query<T> buildSingleEntityLoadQuery(DynamoDBMapper dynamoDBMapper)
	{
		return new SingleEntityLoadByHashAndRangeKeyQuery<T>(dynamoDBMapper,entityInformation.getJavaType(),getHashKeyAttributeValue(),getRangeKeyAttributeValue());
	}
	
	private void checkComparisonOperatorPermittedForCompositeHashAndRangeKey(ComparisonOperator comparisonOperator) {

		if (!ComparisonOperator.EQ.equals(comparisonOperator) && !ComparisonOperator.CONTAINS.equals(comparisonOperator) && !ComparisonOperator.BEGINS_WITH.equals(comparisonOperator) ) {
			throw new UnsupportedOperationException("Only EQ,CONTAINS,BEGINS_WITH supported for composite id comparison");
		}
	
	}
	 
	@SuppressWarnings("unchecked")
	@Override
	public DynamoDBQueryCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value,Class<?> propertyType) {

		
		if (entityInformation.isCompositeHashAndRangeKeyProperty(propertyName))
		{
			checkComparisonOperatorPermittedForCompositeHashAndRangeKey(comparisonOperator);
			Object hashKey = entityInformation.getHashKey((ID)value);
			Object rangeKey = entityInformation.getRangeKey((ID)value);
			if (hashKey != null)
			{
				withSingleValueCriteria(getHashKeyPropertyName(),comparisonOperator,hashKey,hashKey.getClass());
			}
			if (rangeKey != null)
			{
				withSingleValueCriteria(getRangeKeyPropertyName(),comparisonOperator,rangeKey,rangeKey.getClass());
			}
			return this;
		}
		else
		{
			return super.withSingleValueCriteria(propertyName, comparisonOperator, value,propertyType);
		}
	}

	public DynamoDBQueryExpression<T> buildQueryExpression() {
		DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>();
		
		if (isHashKeySpecified())
		{
			queryExpression.withHashKeyValues(entityInformation.getHashKeyPropotypeEntityForHashKey(getHashKeyPropertyValue()));
			queryExpression.withRangeKeyConditions(new HashMap<String,Condition>());
		}
		
		if (isRangeKeySpecified())
		{
			Condition rangeKeyCondition = createSingleValueCondition(getRangeKeyPropertyName(), ComparisonOperator.EQ, getRangeKeyAttributeValue(),getRangeKeyAttributeValue().getClass(),true);
			queryExpression.withRangeKeyCondition(getRangeKeyAttributeName(), rangeKeyCondition);
			applySortIfSpecified(queryExpression,getRangeKeyAttributeName());		
		}
		else if (isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey())
		{
			
			Entry<String, List<Condition>> singleConditions = attributeConditions.entrySet().iterator().next();

			for (Condition condition : singleConditions.getValue())
			{
				
				queryExpression.withRangeKeyCondition(singleConditions.getKey(), condition);
			}
			applySortIfSpecified(queryExpression,singleConditions.getKey());
		}
		else
		{
			applySortIfSpecified(queryExpression,getRangeKeyAttributeName());
		}
	
		
		return queryExpression;
	}
	
	protected void applySortIfSpecified(DynamoDBQueryExpression<T> queryExpression,String permittedAttributeName)
	{
		if (sort != null)
		{
			for (Order order : sort)
			{
				if (permittedAttributeName.equals(getAttributeName(order.getProperty())))
				{
					queryExpression.setScanIndexForward(order.getDirection().equals(Direction.ASC));
				}
				else
				{
					throw new UnsupportedOperationException("Sorting only possible by " + permittedAttributeName + " for the criteria specified");
				}
			}
		}
	}
	
	
	protected Query<T> buildFinderQuery(DynamoDBMapper dynamoDBMapper)
	{
		if (isApplicableForQuery() )
		{
			return new MultipleEntityQueryExpressionQuery<T>(dynamoDBMapper,entityInformation.getJavaType(),buildQueryExpression());
		}
		else
		{
			return new MultipleEntityScanExpressionQuery<T>(dynamoDBMapper,clazz,buildScanExpression());
		}
	}

	@Override
	public boolean isApplicableForLoad() {
		return attributeConditions.size() == 0 && isHashAndRangeKeySpecified();
	}
	
	protected boolean isHashAndRangeKeySpecified()
	{
		return isHashKeySpecified() && isRangeKeySpecified();
	}
	
	protected boolean isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey()
	{
		return attributeConditions.size() == 1 && (attributeConditions.containsKey(getRangeKeyAttributeName())
				|| getIndexRangeKeyAttributeNames().contains(attributeConditions.keySet().iterator().next()));
	}
	
	
	public boolean isApplicableForQuery() {
		
		List<ComparisonOperator> comparisonOperatorsPermittedForQuery = Arrays.asList(
				new ComparisonOperator[] {ComparisonOperator.EQ,ComparisonOperator.LE,ComparisonOperator.LT,ComparisonOperator.GE,
				ComparisonOperator.GT,ComparisonOperator.BEGINS_WITH, ComparisonOperator.BETWEEN});
	
		// Can only query on subset of Conditions
		for (Collection<Condition> conditions : attributeConditions.values())
		{
			for (Condition condition : conditions)
			{
				if (!comparisonOperatorsPermittedForQuery.contains(ComparisonOperator.fromValue(condition.getComparisonOperator())))
				{
					return false;
				}
			}
		}
		
		
		// Can query on hash key only, or by hash key and one other condition on an range or indexrange key
		return isOnlyHashKeySpecified()  || 
			(isHashKeySpecified() && isOnlyASingleAttributeConditionAndItIsOnEitherRangeOrIndexRangeKey());
		
		
		
	}
	
	public DynamoDBScanExpression buildScanExpression() {
		
		if (sort != null)
		{
			throw new UnsupportedOperationException("Sort not supported for scan expressions");
		}
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		if (isHashKeySpecified())
		{
			scanExpression.addFilterCondition(getHashKeyAttributeName(), createSingleValueCondition(getHashKeyPropertyName(), ComparisonOperator.EQ, getHashKeyAttributeValue(),getHashKeyAttributeValue().getClass(),true));
		}
		if (isRangeKeySpecified())
		{
			scanExpression.addFilterCondition(getRangeKeyAttributeName(), createSingleValueCondition(getRangeKeyPropertyName(), ComparisonOperator.EQ, getRangeKeyAttributeValue(),getRangeKeyAttributeValue().getClass(),true));
		}
		for (Map.Entry<String,List<Condition>> conditionEntry : attributeConditions.entrySet())
		{
			for (Condition condition : conditionEntry.getValue())
			{
				scanExpression.addFilterCondition(conditionEntry.getKey(), condition);
			}
		}
		return scanExpression;
	}
	
	public DynamoDBQueryCriteria<T, ID> withRangeKeyEquals(Object value)
	{
		Assert.notNull(value,"Creating conditions on null range keys not supported: please specify a value for '" + getRangeKeyPropertyName() + "'");

		rangeKeyAttributeValue = getPropertyAttributeValue(getRangeKeyPropertyName(),value);
		return this;
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public DynamoDBQueryCriteria<T, ID> withPropertyEquals(String propertyName, Object value,Class<?> propertyType) {
			if (isHashKeyProperty(propertyName))
			{
				return withHashKeyEquals(value);
			}
			else if (isRangeKeyProperty(propertyName))
			{
				return withRangeKeyEquals(value);
			}
			else if (entityInformation.isCompositeHashAndRangeKeyProperty(propertyName))
			{
				Object hashKey = entityInformation.getHashKey((ID)value);
				Object rangeKey = entityInformation.getRangeKey((ID)value);
				if (hashKey != null)
				{
					withHashKeyEquals(hashKey);
				}
				if (rangeKey != null)
				{
					withRangeKeyEquals(rangeKey);
				}
				return this;
			}
			else
			{
				Condition condition = createSingleValueCondition(propertyName, ComparisonOperator.EQ, value,propertyType,false);
				return withCondition(propertyName,condition);		
			}
		
		}

	@Override
	protected boolean isOnlyHashKeySpecified() {
		return isHashKeySpecified() && attributeConditions.size() == 0 && !isRangeKeySpecified();
	}

	}
