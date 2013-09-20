package org.socialsignin.spring.data.dynamodb.query;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public abstract class AbstractQuery<T> implements Query<T> {

	protected DynamoDBMapper dynamoDBMapper;
	protected Class<T> clazz;
	
	public AbstractQuery(DynamoDBMapper dynamoDBMapper,Class<T> clazz)
	{
		this.dynamoDBMapper = dynamoDBMapper;
		this.clazz = clazz;
	}
	
}
