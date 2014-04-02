package org.socialsignin.spring.data.dynamodb.core;

import java.util.List;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.mapping.event.AfterDeleteEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterLoadEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterQueryEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterSaveEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.AfterScanEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.BeforeDeleteEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.BeforeSaveEvent;
import org.socialsignin.spring.data.dynamodb.mapping.event.DynamoDBMappingEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;

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

public class DynamoDBTemplate implements DynamoDBOperations,ApplicationContextAware {

	private DynamoDBMapper dynamoDBMapper;
	private AmazonDynamoDB amazonDynamoDB;
	private DynamoDBMapperConfig dynamoDBMapperConfig;
	private ApplicationEventPublisher eventPublisher;
	
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
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.eventPublisher = applicationContext;
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
		PaginatedQueryList<T> results = dynamoDBMapper.query(domainClass, queryExpression);
		maybeEmitEvent(new AfterQueryEvent<T>(results));
		return results;
	}

	@Override
	public <T> int count(Class<T> domainClass,
			DynamoDBScanExpression scanExpression) {
		return dynamoDBMapper.count(domainClass, scanExpression);
	}

	@Override
	public <T> T load(Class<T> domainClass, Object hashKey, Object rangeKey) {
		T entity =  dynamoDBMapper.load(domainClass, hashKey,rangeKey);
		maybeEmitEvent(new AfterLoadEvent<Object>(entity));
		return entity;
	}

	@Override
	public <T> T load(Class<T> domainClass, Object hashKey) {
		T entity =  dynamoDBMapper.load(domainClass, hashKey);
		maybeEmitEvent(new AfterLoadEvent<Object>(entity));
		return entity;
	}

	@Override
	public <T> PaginatedScanList<T> scan(Class<T> domainClass,
			DynamoDBScanExpression scanExpression) {
		PaginatedScanList<T> results = dynamoDBMapper.scan(domainClass, scanExpression);
		maybeEmitEvent(new AfterScanEvent<T>(results));
		return results;
	}

	@Override
	public Map<String, List<Object>> batchLoad(Map<Class<?>, List<KeyPair>> itemsToGet) {
		Map<String,List<Object>> results = dynamoDBMapper.batchLoad(itemsToGet);
		for (List<Object> resultList : results.values())
		{
			for (Object entity : resultList)
			{
				maybeEmitEvent(new AfterLoadEvent<Object>(entity));
			}
		}
		return results;
	}

	@Override
	public void save(Object entity) {
		maybeEmitEvent(new BeforeSaveEvent<Object>(entity));
		dynamoDBMapper.save(entity);
		maybeEmitEvent(new AfterSaveEvent<Object>(entity));

	}

	@Override
	public void batchSave(Iterable<?> entities) {
		for (Object entity : entities)
		{
			maybeEmitEvent(new BeforeSaveEvent<Object>(entity));
		}
		dynamoDBMapper.batchSave(entities);
		for (Object entity : entities)
		{
			maybeEmitEvent(new AfterSaveEvent<Object>(entity));
		}
	}

	@Override
	public void delete(Object entity) {
		maybeEmitEvent(new BeforeDeleteEvent<Object>(entity));
		dynamoDBMapper.delete(entity);
		maybeEmitEvent(new AfterDeleteEvent<Object>(entity));

	}

	@Override
	public void batchDelete(Iterable<?> entities) {
		for (Object entity : entities)
		{
			maybeEmitEvent(new BeforeDeleteEvent<Object>(entity));
		}
		dynamoDBMapper.batchDelete(entities);
		for (Object entity : entities)
		{
			maybeEmitEvent(new AfterDeleteEvent<Object>(entity));
		}
		
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
	
	protected <T> void maybeEmitEvent(DynamoDBMappingEvent<T> event) {
		if (null != eventPublisher) {
			eventPublisher.publishEvent(event);
		}
}

	
}
