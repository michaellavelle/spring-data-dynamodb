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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityMetadataSupport<T> implements DynamoDBEntityMetadata<T> {

	private final Class<T> domainType;

	/**
	 * Creates a new {@link DefaultJpaEntityMetadata} for the given domain type.
	 * 
	 * @param domainType
	 *            must not be {@literal null}.
	 */
	public DynamoDBEntityMetadataSupport(Class<T> domainType) {

		Assert.notNull(domainType, "Domain type must not be null!");
		this.domainType = domainType;
	}

	public <ID extends Serializable> DynamoDBEntityInformation<T, ID> getEntityInformation() {

		if (hasCompositeId()) {
			DynamoDBEntityWithCompositeIdMetadataImpl<T, ID> metadata = new DynamoDBEntityWithCompositeIdMetadataImpl<T, ID>(
					domainType);
			return new DynamoDBEntityWithCompositeIdInformationImpl<T, ID>(domainType, metadata);
		} else {
			return new DynamoDBEntityInformationImpl<T, ID>(domainType, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.EntityMetadata#getJavaType()
	 */
	@Override
	public Class<T> getJavaType() {
		return domainType;
	}

	@Override
	public boolean hasCompositeId() {
		final List<Boolean> result = new ArrayList<Boolean>();
		ReflectionUtils.doWithMethods(domainType, new MethodCallback() {
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBRangeKey.class) != null) {
					result.add(Boolean.TRUE);
					return;
				}
			}
		});
		return result.size() > 0;
	}

}
