package org.socialsignin.spring.data.dynamodb.query;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class SingleEntityLoadByHashKeyQuery<T> extends AbstractSingleEntityQuery<T> implements Query<T> {

	private Object hashKey;

	
	public SingleEntityLoadByHashKeyQuery(DynamoDBMapper dynamoDBMapper,Class<T> clazz,Object hashKey) {
		super(dynamoDBMapper,clazz);
		this.hashKey = hashKey;
	}

	@Override
	public T getSingleResult() {
		return dynamoDBMapper.load(clazz, hashKey);
	}

}
