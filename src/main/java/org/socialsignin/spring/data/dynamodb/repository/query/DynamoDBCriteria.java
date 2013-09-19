package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBHashAndRangeKey;
import org.springframework.data.domain.Sort;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;

public interface DynamoDBCriteria<T,ID extends Serializable> {
	
	public DynamoDBCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value);

	public DynamoDBCriteria<T, ID> withNoValuedCriteria(String segment, ComparisonOperator null1);

	public DynamoDBCriteria<T, ID> withPropertyEquals(String segment, Object next);

	public DynamoDBCriteria<T, ID> withSort(Sort sort);
	
	public DynamoDBHashAndRangeKey buildLoadCriteria();
	
	public DynamoDBQueryExpression<T> buildQueryExpression();
	
	public DynamoDBScanExpression buildScanExpression();


}
