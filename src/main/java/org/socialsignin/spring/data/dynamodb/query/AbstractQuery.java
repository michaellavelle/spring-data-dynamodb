package org.socialsignin.spring.data.dynamodb.query;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public abstract class AbstractQuery<T> implements Query<T> {

	protected DynamoDBMapper dynamoDBMapper;
	protected Class<T> clazz;
	protected boolean scanEnabled = false;
	
	public void setScanEnabled(boolean scanEnabled) {
		this.scanEnabled = scanEnabled;
	}

	public boolean isScanEnabled() {
		return scanEnabled;
	}

	public AbstractQuery(DynamoDBMapper dynamoDBMapper,Class<T> clazz)
	{
		this.dynamoDBMapper = dynamoDBMapper;
		this.clazz = clazz;
	}
	
}
