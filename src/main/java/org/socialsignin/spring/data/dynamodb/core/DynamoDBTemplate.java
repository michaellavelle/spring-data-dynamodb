package org.socialsignin.spring.data.dynamodb.core;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.Select;

public class DynamoDBTemplate implements DynamoDBOperations {

	private DynamoDBMapper dynamoDBMapper;
	private AmazonDynamoDB amazonDynamoDB;
	private DynamoDBMapperConfig dynamoDBMapperConfig;
	
	public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB,DynamoDBMapperConfig dynamoDBMapperConfig)
	{
		this.amazonDynamoDB = amazonDynamoDB;
		setDynamoDBMapperConfig(dynamoDBMapperConfig);
	}
	
	public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB)
	{
		this.amazonDynamoDB = amazonDynamoDB;
		setDynamoDBMapperConfig(null);

		
	}
	
	public void setDynamoDBMapperConfig(DynamoDBMapperConfig dynamoDBMapperConfig)
	{
		this.dynamoDBMapperConfig = dynamoDBMapperConfig;
		dynamoDBMapper = dynamoDBMapperConfig == null ? new DynamoDBMapper(amazonDynamoDB) : new DynamoDBMapper(
				amazonDynamoDB, dynamoDBMapperConfig);
		if (dynamoDBMapperConfig == null)
		{
			dynamoDBMapperConfig = DynamoDBMapperConfig.DEFAULT;
		}
	}
	
	@Override
	public <T> int count(Class<T> domainClass,
			DynamoDBQueryExpression<T> queryExpression) {
		return dynamoDBMapper.count(domainClass, queryExpression);
	}

	@Override
	public <T> PaginatedQueryList<T> query(Class<T> domainClass,
			DynamoDBQueryExpression<T> queryExpression) {
		return dynamoDBMapper.query(domainClass, queryExpression);
	}

	@Override
	public <T> int count(Class<T> domainClass,
			DynamoDBScanExpression scanExpression) {
		return dynamoDBMapper.count(domainClass, scanExpression);
	}

	@Override
	public <T> T load(Class<T> domainClass, Object hashKey, Object rangeKey) {
		return dynamoDBMapper.load(domainClass, hashKey,rangeKey);
	}

	@Override
	public <T> T load(Class<T> domainClass, Object hashKey) {
		return dynamoDBMapper.load(domainClass, hashKey);
	}

	@Override
	public <T> PaginatedScanList<T> scan(Class<T> domainClass,
			DynamoDBScanExpression scanExpression) {
		return dynamoDBMapper.scan(domainClass, scanExpression);
	}

	@Override
	public Map<String, List<Object>> batchLoad(Map<Class<?>, List<KeyPair>> itemsToGet) {
		return dynamoDBMapper.batchLoad(itemsToGet);
	}

	@Override
	public void save(Object entity) {
		dynamoDBMapper.save(entity);
	}

	@Override
	public void batchSave(Iterable<?> entities) {
		dynamoDBMapper.batchSave(entities);
	}

	@Override
	public void delete(Object entity) {
		dynamoDBMapper.delete(entity);
	}

	@Override
	public void batchDelete(Iterable<?> entities) {
		dynamoDBMapper.batchDelete(entities);
		
	}

	@Override
	public <T> PaginatedQueryList<T> query(Class<T> clazz,
			QueryRequest queryRequest) {
		QueryResult queryResult = amazonDynamoDB.query(queryRequest);
		return new PaginatedQueryList<T>(dynamoDBMapper, clazz, amazonDynamoDB, queryRequest, queryResult,
				dynamoDBMapperConfig.getPaginationLoadingStrategy(), dynamoDBMapperConfig);
	}

	@Override
	public <T> int count(Class<T> clazz, QueryRequest mutableQueryRequest) {
		mutableQueryRequest.setSelect(Select.COUNT);

        // Count queries can also be truncated for large datasets
        int count = 0;
        QueryResult queryResult = null;
        do {
            queryResult = amazonDynamoDB.query(mutableQueryRequest);
            count += queryResult.getCount();
            mutableQueryRequest.setExclusiveStartKey(queryResult.getLastEvaluatedKey());
        } while (queryResult.getLastEvaluatedKey() != null);

        return count;
	}

	@Override
	public String getOverriddenTableName(String tableName) {
		if (dynamoDBMapperConfig.getTableNameOverride() != null) {
			if (dynamoDBMapperConfig.getTableNameOverride().getTableName() != null) {
				tableName = dynamoDBMapperConfig.getTableNameOverride().getTableName();
			} else {
				tableName = dynamoDBMapperConfig.getTableNameOverride().getTableNamePrefix() + tableName;
			}
		}

		return tableName;
	}

}
