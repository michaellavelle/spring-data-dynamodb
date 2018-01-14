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

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.query.DynamoDBQueryLookupStrategy;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import java.io.Serializable;
import java.util.Optional;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

/**
 * @author Michael Lavelle
 */
public class DynamoDBRepositoryFactory extends RepositoryFactorySupport {

	private final DynamoDBOperations dynamoDBOperations;

	public DynamoDBRepositoryFactory(DynamoDBOperations dynamoDBOperations) {
		this.dynamoDBOperations = dynamoDBOperations;

	}

	@Override
	public <T, ID> DynamoDBEntityInformation<T, ID> getEntityInformation(final Class<T> domainClass) {

		final DynamoDBEntityMetadataSupport<T, ID> metadata = new DynamoDBEntityMetadataSupport<T, ID>(domainClass);
		return metadata.getEntityInformation();
	}

	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider) {
		return Optional.of(DynamoDBQueryLookupStrategy.create(dynamoDBOperations, key));
	}

	/**
	 * Callback to create a {@link DynamoDBCrudRepository} instance with the given {@link RepositoryMetadata}
	 *
	 * @param <T>
	 * @param <ID>
	 * @param metadata
	 * @see #getTargetRepository(RepositoryInformation)
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T, ID extends Serializable> DynamoDBCrudRepository<?, ?> getDynamoDBRepository(
			RepositoryMetadata metadata) {
		return new SimpleDynamoDBPagingAndSortingRepository(getEntityInformation(metadata.getDomainType()),
				dynamoDBOperations, getEnableScanPermissions(metadata));
	}

	protected EnableScanPermissions getEnableScanPermissions(RepositoryMetadata metadata) {
		return new EnableScanAnnotationPermissions(metadata.getRepositoryInterface());
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslRepository(metadata.getRepositoryInterface())) {
			throw new IllegalArgumentException("QueryDsl Support has not been implemented yet.");
		}
		return SimpleDynamoDBPagingAndSortingRepository.class;
	}

	private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QuerydslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation metadata) {
		return getDynamoDBRepository(metadata);
	}

}
