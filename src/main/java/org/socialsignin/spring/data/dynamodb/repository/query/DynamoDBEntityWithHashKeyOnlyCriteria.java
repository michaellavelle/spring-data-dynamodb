package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.query.MultipleEntityScanExpressionQuery;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.query.SingleEntityLoadByHashKeyQuery;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

public class DynamoDBEntityWithHashKeyOnlyCriteria<T,ID extends Serializable> extends AbstractDynamoDBQueryCriteria<T, ID> {

	
	public DynamoDBEntityWithHashKeyOnlyCriteria(DynamoDBEntityInformation<T,ID> entityInformation) {
		super(entityInformation);

	}
	
	protected Query<T> buildSingleEntityLoadQuery(DynamoDBMapper dynamoDBMapper)
	{
		return new SingleEntityLoadByHashKeyQuery<T>(dynamoDBMapper,clazz,getHashKeyAttributeValue());
	}
	
	protected Query<T> buildFinderQuery(DynamoDBMapper dynamoDBMapper)
	{
		return new MultipleEntityScanExpressionQuery<T>(dynamoDBMapper,clazz,buildScanExpression());
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
		
		if (sort != null)
		{
			throw new UnsupportedOperationException("Sort not supported for scan expressions");
		}
		
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		if (isHashKeySpecified())
		{
			scanExpression.addFilterCondition(getHashKeyAttributeName(), createSingleValueCondition(getHashKeyPropertyName(), ComparisonOperator.EQ, getHashKeyAttributeValue()));
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
	
	
	
	@Override
	public DynamoDBQueryCriteria<T, ID> withPropertyEquals(String propertyName, Object value) {
			if (isHashKeyProperty(propertyName))
			{
				return withHashKeyEquals(value);
			}
			else
			{
				Condition condition = createSingleValueCondition(propertyName, ComparisonOperator.EQ, value);
				return withCondition(propertyName,condition);	
			}
		}

	

	}
