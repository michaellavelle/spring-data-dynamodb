package org.socialsignin.spring.data.dynamodb.query;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class SingleEntityLoadByHashAndRangeKeyQuery<T> extends AbstractSingleEntityQuery<T> implements Query<T> {

	private Object hashKey;
	private Object rangeKey;

	
	public SingleEntityLoadByHashAndRangeKeyQuery(DynamoDBMapper dynamoDBMapper,Class<T> clazz,Object hashKey,Object rangeKey) {
		super(dynamoDBMapper,clazz);
		this.hashKey = hashKey;
		this.rangeKey = rangeKey;
	}
	@Override
	public T getSingleResult() {
		return dynamoDBMapper.load(clazz, hashKey, rangeKey);
	}

}
