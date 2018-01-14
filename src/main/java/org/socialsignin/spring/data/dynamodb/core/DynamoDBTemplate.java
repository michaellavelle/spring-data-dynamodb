package org.socialsignin.spring.data.dynamodb.core;

import java.util.Iterator;
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
import org.springframework.util.Assert;

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

	private final DynamoDBMapper dynamoDBMapper;
	private final AmazonDynamoDB amazonDynamoDB;
	private final DynamoDBMapperConfig dynamoDBMapperConfig;
	private ApplicationEventPublisher eventPublisher;
	
	/** Convenient constructor to use the default {@link DynamoDBMapper#DynamoDBMapper(AmazonDynamoDB)} */
	public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig dynamoDBMapperConfig)
	{
	    this(amazonDynamoDB, dynamoDBMapperConfig, null);
	}
    
	/** Convenient constructor to use the {@link DynamoDBMapperConfig#DEFAULT} */

    public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper)
    {
        this(amazonDynamoDB, null, dynamoDBMapper);
    }
    
    /** Convenient construcotr to thse the {@link DynamoDBMapperConfig#DEFAULT} and default {@link DynamoDBMapper#DynamoDBMapper(AmazonDynamoDB)} */
    public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB)
    {
        this(amazonDynamoDB, null, null);
    }
	
    /** Initializes a new {@code DynamoDBTemplate}.
     * The following combinations are valid:
     * @param amazonDynamoDB must not be {@code null}
     * @param dynamoDBMapperConfig can be {@code null} - {@link DynamoDBMapperConfig#DEFAULT} is used if {@code null} is passed in
     * @param dynamoDBMapper can be {@code null} - {@link DynamoDBMapper#DynamoDBMapper(AmazonDynamoDB, DynamoDBMapperConfig)} is used if {@code null} is passed in */
	public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig dynamoDBMapperConfig, DynamoDBMapper dynamoDBMapper) {
       Assert.notNull(amazonDynamoDB, "amazonDynamoDB must not be null!");
	   this.amazonDynamoDB = amazonDynamoDB;
	   
	  if (dynamoDBMapperConfig == null) {
		  this.dynamoDBMapperConfig = DynamoDBMapperConfig.DEFAULT;		  
	  } else {
		  this.dynamoDBMapperConfig = dynamoDBMapperConfig;
	  }
	  
	  if (dynamoDBMapper == null) {
          this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
	  } else {
          this.dynamoDBMapper = dynamoDBMapper;
	  }
    }
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.eventPublisher = applicationContext;
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
		if (entity != null)
		{
			maybeEmitEvent(new AfterLoadEvent<Object>(entity));
		}
		return entity;
	}

	@Override
	public <T> T load(Class<T> domainClass, Object hashKey) {
		T entity =  dynamoDBMapper.load(domainClass, hashKey);
		if (entity != null)
		{
			maybeEmitEvent(new AfterLoadEvent<Object>(entity));
		}
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
	@Deprecated
	public void batchSave(List<?> entities) {
		Iterable<?> iterableEntities = entities;
		batchSave(iterableEntities);
	}
	
	@Override
        public void batchSave(Iterable<?> entities) {
	        Iterator<?> iteratorBefore = entities.iterator();
	        while( iteratorBefore.hasNext() ){
	            maybeEmitEvent(new BeforeSaveEvent<Object>(iteratorBefore.next()));
	        }
                dynamoDBMapper.batchSave(entities);
                Iterator<?> iteratorAfter = entities.iterator();
                while( iteratorAfter.hasNext() ){
                    maybeEmitEvent(new BeforeSaveEvent<Object>(iteratorAfter.next()));
                }
        }

	@Override
	public void delete(Object entity) {
		maybeEmitEvent(new BeforeDeleteEvent<Object>(entity));
		dynamoDBMapper.delete(entity);
		maybeEmitEvent(new AfterDeleteEvent<Object>(entity));

	}

	@Override
	@Deprecated
	public void batchDelete(List<?> entities) {
	        Iterable<?> iterableEntities = entities;
	        batchDelete(iterableEntities);
	}
	
	@Override
	public void batchDelete(Iterable<?> entities) {
	    Iterator<?> iteratorBefore = entities.iterator();
            while( iteratorBefore.hasNext() ){
                maybeEmitEvent(new BeforeDeleteEvent<Object>(iteratorBefore.next()));
            }
	    dynamoDBMapper.batchDelete(entities);
	    Iterator<?> iteratorAfter = entities.iterator();
            while( iteratorAfter.hasNext() ){
                maybeEmitEvent(new AfterDeleteEvent<Object>(iteratorAfter.next()));
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
	public <T> String getOverriddenTableName(Class<T> domainClass, String tableName) {
		if (dynamoDBMapperConfig.getTableNameOverride() != null) {
			if (dynamoDBMapperConfig.getTableNameOverride().getTableName() != null) {
				tableName = dynamoDBMapperConfig.getTableNameOverride().getTableName();
			} else {
				tableName = dynamoDBMapperConfig.getTableNameOverride().getTableNamePrefix() + tableName;
			}
		} else if (dynamoDBMapperConfig.getTableNameResolver() != null) {
		  tableName = dynamoDBMapperConfig.getTableNameResolver().getTableName(domainClass, dynamoDBMapperConfig);
		}

		return tableName;
	}
	
	protected <T> void maybeEmitEvent(DynamoDBMappingEvent<T> event) {
		if (null != eventPublisher) {
			eventPublisher.publishEvent(event);
		}
}

	
}
