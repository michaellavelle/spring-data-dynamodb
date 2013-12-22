/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.cdi;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.socialsignin.spring.data.dynamodb.query.QueryRequestMapper;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBRepositoryFactory;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

/**
 * A bean which represents a DynamoDB repository.
 * 
 * @author Michael Lavelle
 * @param <T>
 *            The type of the repository.
 */
class DynamoDBRepositoryBean<T> extends CdiRepositoryBean<T> {

	private final Bean<AmazonDynamoDB> amazonDynamoDBBean;

	private final Bean<DynamoDBMapperConfig> dynamoDBMapperConfigBean;

	/**
	 * Constructs a {@link DynamoDBRepositoryBean}.
	 * 
	 * @param beanManager
	 *            must not be {@literal null}.
	 * @param dynamoDBMapperBean
	 *            must not be {@literal null}.
	 * @param qualifiers
	 *            must not be {@literal null}.
	 * @param repositoryType
	 *            must not be {@literal null}.
	 */
	DynamoDBRepositoryBean(BeanManager beanManager, Bean<AmazonDynamoDB> amazonDynamoDBBean,
			Bean<DynamoDBMapperConfig> dynamoDBMapperConfigBean, Set<Annotation> qualifiers, Class<T> repositoryType) {

		super(qualifiers, repositoryType, beanManager);

		Assert.notNull(amazonDynamoDBBean);
		this.amazonDynamoDBBean = amazonDynamoDBBean;
		this.dynamoDBMapperConfigBean = dynamoDBMapperConfigBean;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.enterprise.context.spi.Contextual#create(javax.enterprise
	 * .context.spi.CreationalContext)
	 */
	@Override
	public T create(CreationalContext<T> creationalContext, Class<T> repositoryType) {

		// Get an instance from the associated AmazonDynamoDB bean.
		AmazonDynamoDB amazonDynamoDB = getDependencyInstance(amazonDynamoDBBean, AmazonDynamoDB.class);

		// Get an instance from the associated optional AmazonDynamoDB bean.
		DynamoDBMapperConfig dynamoDBMapperConfig = dynamoDBMapperConfigBean == null ? null : getDependencyInstance(
				dynamoDBMapperConfigBean, DynamoDBMapperConfig.class);

		// Create the DynamoDB repository instance and return it.
		DynamoDBMapper dynamoDBMapper = dynamoDBMapperConfig == null ? new DynamoDBMapper(amazonDynamoDB) : new DynamoDBMapper(
				amazonDynamoDB, dynamoDBMapperConfig);

		DynamoDBRepositoryFactory factory = new DynamoDBRepositoryFactory(dynamoDBMapper, new QueryRequestMapper(amazonDynamoDB,
				dynamoDBMapperConfig, dynamoDBMapper));
		return factory.getRepository(repositoryType);
	}
}
