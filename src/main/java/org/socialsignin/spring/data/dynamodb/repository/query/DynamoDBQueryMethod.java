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
package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityMetadataSupport;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
/**
 * @author Michael Lavelle
 */
public class DynamoDBQueryMethod<T, ID extends Serializable> extends
		QueryMethod {

	private final Method method;
	private final boolean scanEnabledForRepository;

	public DynamoDBQueryMethod(Method method, RepositoryMetadata metadata) {
		super(method, metadata);
		this.method = method;
		this.scanEnabledForRepository = metadata.getRepositoryInterface().isAnnotationPresent(EnableScan.class);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.query.QueryMethod#getEntityInformation
	 * ()
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DynamoDBEntityInformation<T, ID> getEntityInformation() {
		return new DynamoDBEntityMetadataSupport(getDomainClass())
				.getEntityInformation();
	}

	public Class<T> getEntityType() {

		return getEntityInformation().getJavaType();
	}

}
