/**
 * Copyright © 2013 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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
package org.socialsignin.spring.data.dynamodb.repository.support;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBTemplate;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Special adapter for Springs
 * {@link org.springframework.beans.factory.FactoryBean} interface to allow easy
 * setup of repository factories via Spring configuration.
 * 
 * @author Michael Lavelle
 * @param <T>
 *            the type of the repository
 */
public class DynamoDBRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
    extends RepositoryFactoryBeanSupport<T, S, ID> implements ApplicationContextAware {

	private DynamoDBMapperConfig dynamoDBMapperConfig;

	private AmazonDynamoDB amazonDynamoDB;
	
	private DynamoDBOperations dynamoDBOperations;

	private ApplicationContext applicationContext;

	public DynamoDBRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

	public void setAmazonDynamoDB(AmazonDynamoDB amazonDynamoDB) {
		this.amazonDynamoDB = amazonDynamoDB;
		setMappingContext(new DynamoDBMappingContext());

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		if (dynamoDBOperations == null)
		{
			/**
			 * The ApplicationContextAware within DynamoDBTemplate is not executed as
			 * DynamoDBTemplate is not initialized as a bean
			 */
			DynamoDBTemplate dynamoDBTemplate = new DynamoDBTemplate(amazonDynamoDB,dynamoDBMapperConfig);
			dynamoDBTemplate.setApplicationContext(applicationContext);
			dynamoDBOperations = dynamoDBTemplate;
		}
		return new DynamoDBRepositoryFactory(dynamoDBOperations);
	}

	public void setDynamoDBMapperConfig(DynamoDBMapperConfig dynamoDBMapperConfig) {
		this.dynamoDBMapperConfig = dynamoDBMapperConfig;
	}
	
	public void setDynamoDBOperations(DynamoDBOperations dynamoDBOperations) {
		this.dynamoDBOperations = dynamoDBOperations;
		setMappingContext(new DynamoDBMappingContext());

	}
}
