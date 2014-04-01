package org.socialsignin.spring.data.dynamodb.core;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;

public interface DynamoDBOperations {

	public <T> int count(Class<T> domainClass,DynamoDBQueryExpression<T> queryExpression);
	public <T> int count(Class<T> domainClass,DynamoDBScanExpression scanExpression);
	public <T> int count(Class<T> clazz, QueryRequest mutableQueryRequest);

	
	public <T> PaginatedQueryList<T> query(Class<T> clazz, QueryRequest queryRequest);
	public <T> PaginatedQueryList<T> query(Class<T> domainClass,DynamoDBQueryExpression<T> queryExpression);
	public <T> PaginatedScanList<T> scan(Class<T> domainClass,DynamoDBScanExpression scanExpression);

	public <T> T load(Class<T> domainClass,Object hashKey,Object rangeKey);
	public <T> T load(Class<T> domainClass,Object hashKey);
	public Map<String, List<Object>> batchLoad(Map<Class<?>, List<KeyPair>> itemsToGet);

	public void save(Object entity);
	public void batchSave(Iterable<?> entities);

	public void delete(Object entity);
	public void batchDelete(Iterable<?> entities);

	public String getOverriddenTableName(String tableName);


}
