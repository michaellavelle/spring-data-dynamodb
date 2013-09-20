package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;

import org.socialsignin.spring.data.dynamodb.query.Query;
import org.springframework.data.domain.Sort;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;

public interface DynamoDBQueryCriteria<T,ID extends Serializable> {
	
	public DynamoDBQueryCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value);

	public DynamoDBQueryCriteria<T, ID> withNoValuedCriteria(String segment, ComparisonOperator null1);

	public DynamoDBQueryCriteria<T, ID> withPropertyEquals(String segment, Object next);

	public DynamoDBQueryCriteria<T, ID> withSort(Sort sort);	
	
	public Query<T> buildQuery(DynamoDBMapper dynamoDBMapper);


}
