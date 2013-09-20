package org.socialsignin.spring.data.dynamodb.query;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

public class MultipleEntityScanExpressionQuery<T> extends AbstractMultipleEntityQuery<T> {

	private DynamoDBScanExpression scanExpression;
	
	public MultipleEntityScanExpressionQuery(DynamoDBMapper dynamoDBMapper, Class<T> clazz,DynamoDBScanExpression scanExpression) {
		super(dynamoDBMapper, clazz);
		this.scanExpression = scanExpression;
	}

	@Override
	public List<T> getResultList() {
		return dynamoDBMapper.scan(clazz,scanExpression);
	}

}
