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
package org.socialsignin.spring.data.dynamodb.repository.query;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityMetadataSupport;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBQueryMethod<T, ID> extends QueryMethod {

	private final Method method;
	private final boolean scanEnabledForRepository;
	private final boolean scanCountEnabledForRepository;
	private final Optional<String> projectionExpression;

	public DynamoDBQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
		super(method, metadata, factory);
		this.method = method;
		this.scanEnabledForRepository = metadata.getRepositoryInterface().isAnnotationPresent(EnableScan.class);
		this.scanCountEnabledForRepository = metadata.getRepositoryInterface()
				.isAnnotationPresent(EnableScanCount.class);

		Query query = method.getAnnotation(Query.class);
		if (query != null) {
			String projections = query.fields();
			if (!StringUtils.isEmpty(projections)) {
				this.projectionExpression = Optional.of(query.fields());
			} else {
				this.projectionExpression = Optional.empty();
			}
		} else {
			this.projectionExpression = Optional.empty();
		}
	}

	/**
	 * Returns the actual return type of the method.
	 * 
	 * @return
	 */
	Class<?> getReturnType() {

		return method.getReturnType();
	}

	public boolean isScanEnabled() {
		return scanEnabledForRepository || method.isAnnotationPresent(EnableScan.class);
	}

	public boolean isScanCountEnabled() {
		return scanCountEnabledForRepository || method.isAnnotationPresent(EnableScanCount.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.query.QueryMethod#getEntityInformation ()
	 */
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public DynamoDBEntityInformation<T, ID> getEntityInformation() {
		return new DynamoDBEntityMetadataSupport(getDomainClass()).getEntityInformation();
	}

	public Class<T> getEntityType() {

		return getEntityInformation().getJavaType();
	}

	public Optional<String> getProjectionExpression() {
		return this.projectionExpression;
	}

}
