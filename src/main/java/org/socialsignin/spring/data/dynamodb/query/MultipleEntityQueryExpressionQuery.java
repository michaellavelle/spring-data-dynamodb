package org.socialsignin.spring.data.dynamodb.query;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

public class MultipleEntityQueryExpressionQuery<T> extends AbstractMultipleEntityQuery<T> {

	private DynamoDBQueryExpression<T> queryExpression;
	
	public MultipleEntityQueryExpressionQuery(DynamoDBMapper dynamoDBMapper, Class<T> clazz,DynamoDBQueryExpression<T> queryExpression) {
		super(dynamoDBMapper, clazz);
		this.queryExpression = queryExpression;
	}

	@Override
	public List<T> getResultList() {
		return dynamoDBMapper.query(clazz, queryExpression);
	}

}
