package org.socialsignin.spring.data.dynamodb.query;

import java.util.List;

import org.springframework.util.Assert;


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
		assertScanEnabled(isScanEnabled());
		return dynamoDBMapper.scan(clazz,scanExpression);
	}
	
	public void assertScanEnabled(boolean scanEnabled)
	{
		Assert.isTrue(scanEnabled,"Scanning for this query is not enabled.  " +
				"To enable annotate your repository method with @EnableScan, or " +
				"enable scanning for all repository methods by annotating your repository interface with @EnableScan");
	}

}
