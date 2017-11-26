package org.socialsignin.spring.data.dynamodb.core;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;

import java.util.List;
import java.util.Map;

public interface DynamoDBOperations {

	<T> int count(Class<T> domainClass,DynamoDBQueryExpression<T> queryExpression);
	<T> int count(Class<T> domainClass,DynamoDBScanExpression scanExpression);
	<T> int count(Class<T> clazz, QueryRequest mutableQueryRequest);

	
	<T> PaginatedQueryList<T> query(Class<T> clazz, QueryRequest queryRequest);
	<T> PaginatedQueryList<T> query(Class<T> domainClass, DynamoDBQueryExpression<T> queryExpression);
	<T> PaginatedScanList<T> scan(Class<T> domainClass, DynamoDBScanExpression scanExpression);

	<T> T load(Class<T> domainClass,Object hashKey, Object rangeKey);
	<T> T load(Class<T> domainClass,Object hashKey);
	Map<String, List<Object>> batchLoad(Map<Class<?>, List<KeyPair>> itemsToGet);

	void save(Object entity);
	void batchSave(List<?> entities);
	void batchSave(Iterable<?> entities);

	void delete(Object entity);
	void batchDelete(List<?> entities);
    void batchDelete(Iterable<?> entities);

	<T> String getOverriddenTableName(Class<T> domainClass, String tableName);

    /**
     * Provides access to the DynamoDB mapper table model of the underlying domain type.
     *
	 * @param <T> The type of the domain type itself
     * @param domainClass A domain type
     * @return Corresponding DynamoDB table model
     */
    <T> DynamoDBMapperTableModel<T> getTableModel(Class<T> domainClass);
}
