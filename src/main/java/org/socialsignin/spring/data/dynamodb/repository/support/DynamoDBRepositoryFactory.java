/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.support;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

import java.io.Serializable;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
/**
 * @author Michael Lavelle
 */
public class DynamoDBRepositoryFactory extends RepositoryFactorySupport {

	private DynamoDBMapper dynamoDBMapper;

	public DynamoDBRepositoryFactory(DynamoDBMapper dynamoDBMapper) {
		this.dynamoDBMapper = dynamoDBMapper;
	}

	@Override
	public <T, ID extends Serializable> DynamoDBEntityInformation<T, ID> getEntityInformation(final Class<T> domainClass) {

		DynamoDBEntityMetadataSupport<T> metadata = new DynamoDBEntityMetadataSupport<T>(domainClass);
		return metadata.getEntityInformation();
	}

	/**
	 * Callback to create a {@link JpaRepository} instance with the given
	 * {@link EntityManager}
	 * 
	 * @param <T>
	 * @param <ID>
	 * @param entityManager
	 * @see #getTargetRepository(RepositoryMetadata)
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T, ID extends Serializable> DynamoDBCrudRepository<?, ?> getDynamoDBRepository(
			RepositoryMetadata metadata) {

		return new SimpleDynamoDBCrudRepository(getEntityInformation(metadata.getDomainType()), dynamoDBMapper);
	}

	@Override
	protected Object getTargetRepository(RepositoryMetadata metadata) {

		return getDynamoDBRepository(metadata);

	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslRepository(metadata.getRepositoryInterface())) {
			throw new IllegalArgumentException("QueryDsl Support has not been implemented yet.");
		}
		return SimpleDynamoDBCrudRepository.class;
	}

	private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

}
