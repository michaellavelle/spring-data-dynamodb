/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.core;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DynamoDBTemplate implements DynamoDBOperations, ApplicationContextAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBTemplate.class);
	private final DynamoDBMapper dynamoDBMapper;
	private final AmazonDynamoDB amazonDynamoDB;
	private final DynamoDBMapperConfig dynamoDBMapperConfig;
	private ApplicationEventPublisher eventPublisher;
	
	/** Convenient constructor to use the default {@link DynamoDBMapper#DynamoDBMapper(AmazonDynamoDB)}
	 * @param amazonDynamoDB The AWS SDK instance to talk to DynamoDB
	 * @param dynamoDBMapperConfig The config to use
	 * */
	public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig dynamoDBMapperConfig)
	{
	    this(amazonDynamoDB, dynamoDBMapperConfig, null);
	}
    
	/** Convenient constructor to use the {@link DynamoDBMapperConfig#DEFAULT}
	 * @param amazonDynamoDB The AWS SDK instance to talk to DynamoDB
	 * @param dynamoDBMapper The Mapper to use
	 * */
    public DynamoDBTemplate(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper)
    {
        this(amazonDynamoDB, null, dynamoDBMapper);
    }
    
    /** Convenient construcotr to thse the {@link DynamoDBMapperConfig#DEFAULT} and default {@link DynamoDBMapper#DynamoDBMapper(AmazonDynamoDB)}
	 * @param amazonDynamoDB The AWS SDK instance to talk to DynamoDB
	 * */
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

			// #146, #81 #157
			// Trying to fix half-initialized DynamoDBMapperConfigs here.
			// The old documentation advised to start with an empty builder. Therefore we
			// try here to set required fields to their defaults -
			// As the documentation at https://github.com/derjust/spring-data-dynamodb/wiki/Alter-table-name-during-runtime (same as https://git.io/DynamoDBMapperConfig)
			// now does: Start with #DEFAULT and add what's required
			DynamoDBMapperConfig.Builder emptyBuilder = DynamoDBMapperConfig.builder(); // empty (!) builder

			if (dynamoDBMapperConfig.getConversionSchema() == null) {
				LOGGER.warn("No ConversionSchema set in the provided dynamoDBMapperConfig! Merging with DynamoDBMapperConfig.DEFAULT - Please see https://git.io/DynamoDBMapperConfig");
				// DynamoDBMapperConfig#DEFAULT comes with a  ConversionSchema
				emptyBuilder.withConversionSchema(DynamoDBMapperConfig.DEFAULT.getConversionSchema());
			}

			if (dynamoDBMapperConfig.getTypeConverterFactory() == null) {
				LOGGER.warn("No TypeConverterFactory set in the provided dynamoDBMapperConfig! Merging with DynamoDBMapperConfig.DEFAULT - Please see https://git.io/DynamoDBMapperConfig");
				// DynamoDBMapperConfig#DEFAULT comes with a TypeConverterFactory
				emptyBuilder.withTypeConverterFactory(DynamoDBMapperConfig.DEFAULT.getTypeConverterFactory());
			}

			// Deprecated but the only way how DynamoDBMapperConfig#merge is exposed
			this.dynamoDBMapperConfig = new DynamoDBMapperConfig(dynamoDBMapperConfig, emptyBuilder.build());
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
		maybeEmitEvent(results, AfterQueryEvent::new);
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
		maybeEmitEvent(entity, AfterLoadEvent::new);

		return entity;
	}

	@Override
	public <T> T load(Class<T> domainClass, Object hashKey) {
		T entity =  dynamoDBMapper.load(domainClass, hashKey);
		maybeEmitEvent(entity, AfterLoadEvent::new);

		return entity;
	}

	@Override
	public <T> PaginatedScanList<T> scan(Class<T> domainClass,
			DynamoDBScanExpression scanExpression) {
		PaginatedScanList<T> results = dynamoDBMapper.scan(domainClass, scanExpression);
		maybeEmitEvent(results, AfterScanEvent::new);
		return results;
	}

	@Override
	public Map<String, List<Object>> batchLoad(Map<Class<?>, List<KeyPair>> itemsToGet) {
		Map<String,List<Object>> results = dynamoDBMapper.batchLoad(itemsToGet);
		for (List<Object> resultList : results.values())
		{
			for (Object entity : resultList)
			{
				maybeEmitEvent(entity, AfterLoadEvent::new);
			}
		}
		return results;
	}

	@Override
	public <T> T save(T entity) {
		maybeEmitEvent(entity, BeforeSaveEvent::new);
		dynamoDBMapper.save(entity);
		maybeEmitEvent(entity, AfterSaveEvent::new);
		return entity;

	}
	
	@Override
	public List<FailedBatch> batchSave(Iterable<?> entities) {
		entities.forEach(it -> maybeEmitEvent(it, BeforeSaveEvent::new));

		List<FailedBatch> result = dynamoDBMapper.batchSave(entities);

		entities.forEach(it -> maybeEmitEvent(it, AfterSaveEvent::new));
        return result;
    }

	@Override
	public <T> T delete(T entity) {
		maybeEmitEvent(entity, BeforeDeleteEvent::new);
		dynamoDBMapper.delete(entity);
		maybeEmitEvent(entity, AfterDeleteEvent::new);
		return entity;
	}
	
	@Override
	public List<FailedBatch> batchDelete(Iterable<?> entities) {
		entities.forEach(it -> maybeEmitEvent(it, BeforeDeleteEvent::new));

	    List<FailedBatch> result = dynamoDBMapper.batchDelete(entities);

		entities.forEach(it -> maybeEmitEvent(it, AfterDeleteEvent::new));
		return result;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> DynamoDBMapperTableModel<T> getTableModel(Class<T> domainClass) {
        return dynamoDBMapper.getTableModel(domainClass, dynamoDBMapperConfig);
    }

    protected <T> void maybeEmitEvent(@Nullable T source, Function<T, DynamoDBMappingEvent<T>> factory) {
    	if (eventPublisher != null) {
    		if (source != null) {
				DynamoDBMappingEvent<T> event = factory.apply(source);

				eventPublisher.publishEvent(event);
			}
		}

	}
}
