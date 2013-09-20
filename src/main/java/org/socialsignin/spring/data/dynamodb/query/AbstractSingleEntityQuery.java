package org.socialsignin.spring.data.dynamodb.query;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public abstract class AbstractSingleEntityQuery<T> extends AbstractQuery<T> implements Query<T> {
	
	public AbstractSingleEntityQuery(DynamoDBMapper dynamoDBMapper,Class<T> clazz) {
		super(dynamoDBMapper,clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getResultList() {
		return Arrays.asList(getSingleResult());
	}
}
