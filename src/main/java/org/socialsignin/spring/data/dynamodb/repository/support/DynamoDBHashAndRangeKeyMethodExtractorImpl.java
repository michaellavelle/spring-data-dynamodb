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

import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

/**
 * @author Michael Lavelle
 */
public class DynamoDBHashAndRangeKeyMethodExtractorImpl<T> implements DynamoDBHashAndRangeKeyMethodExtractor<T> {

	private final Class<T> idType;
	private Method hashKeyMethod;
	private Method rangeKeyMethod;

	/**
	 * Creates a new {@link DefaultJpaEntityMetadata} for the given domain type.
	 * 
	 * @param domainType
	 *            must not be {@literal null}.
	 */
	public DynamoDBHashAndRangeKeyMethodExtractorImpl(final Class<T> idType) {

		Assert.notNull(idType, "Id type must not be null!");
		this.idType = idType;
		ReflectionUtils.doWithMethods(idType, new MethodCallback() {
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBHashKey.class) != null) {
					Assert.isNull(hashKeyMethod, "Multiple methods annotated by @DynamoDBHashKey within type " + idType.getName()
							+ "!");
					ReflectionUtils.makeAccessible(method);
					hashKeyMethod = method;
				}
			}
		});
		ReflectionUtils.doWithMethods(idType, new MethodCallback() {
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBRangeKey.class) != null) {
					Assert.isNull(rangeKeyMethod,
							"Multiple methods annotated by @DynamoDBRangeKey within type " + idType.getName() + "!");
					ReflectionUtils.makeAccessible(method);
					rangeKeyMethod = method;
				}
			}
		});
		Assert.notNull(hashKeyMethod, "No method annotated by @DynamoDBHashKey within type " + idType.getName() + "!");
		Assert.notNull(rangeKeyMethod, "No method annotated by @DynamoDBRangeKey within type " + idType.getName() + "!");

	}

	@Override
	public Class<T> getJavaType() {
		return idType;
	}

	@Override
	public Method getHashKeyMethod() {

		return hashKeyMethod;
	}

	@Override
	public Method getRangeKeyMethod() {
		return rangeKeyMethod;
	}

}
