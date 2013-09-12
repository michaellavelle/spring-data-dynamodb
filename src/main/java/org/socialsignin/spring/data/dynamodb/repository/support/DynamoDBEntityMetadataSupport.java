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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;

/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityMetadataSupport<T, ID extends Serializable> implements DynamoDBEntityMetadata<T, ID> {

	private final Class<T> domainType;

	private Method getHashKeySetterMethod() {
		final List<Method> hashKeySetterMethodList = new ArrayList<Method>();

		ReflectionUtils.doWithMethods(domainType, new MethodCallback() {
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBHashKey.class) != null) {

					String getterMethodName = method.getName();
					String setterMethodName = getterMethodName.replaceAll("get", "set");
					Method setterMethod = ReflectionUtils.findMethod(domainType, setterMethodName, String.class);
					hashKeySetterMethodList.add(setterMethod);
					return;
				}
			}
		});
		return hashKeySetterMethodList.size() == 0 ? null : hashKeySetterMethodList.get(0);
	}

	/**
	 * Creates a new {@link DefaultJpaEntityMetadata} for the given domain type.
	 * 
	 * @param domainType
	 *            must not be {@literal null}.
	 */
	public DynamoDBEntityMetadataSupport(final Class<T> domainType) {

		Assert.notNull(domainType, "Domain type must not be null!");
		this.domainType = domainType;

	}

	public DynamoDBEntityInformation<T, ID> getEntityInformation() {

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

	public boolean isHashKeyProperty(String propertyName) {
		return isPropertyAnnotatedWith(propertyName, DynamoDBHashKey.class);
	}

	public boolean isRangeKeyProperty(String propertyName) {
		return isPropertyAnnotatedWith(propertyName, DynamoDBRangeKey.class);
	}

	private boolean isPropertyAnnotatedWith(final String propertyName, final Class<? extends Annotation> annotation) {
		
		Method method = ReflectionUtils.findMethod(domainType, toMethodName(propertyName));
		return method.getAnnotation(annotation) != null;
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

	private String toMethodName(String propertyName) {
		String methodName = propertyName.substring(0, 1).toUpperCase();
		if (propertyName.length() > 1) {
			methodName = methodName + propertyName.substring(1);
		}
		return "get" + methodName;
	}

	@Override
	public String getOverriddenAttributeName(final String propertyName) {

		Method method = ReflectionUtils.findMethod(domainType, toMethodName(propertyName));
		if (method.getAnnotation(DynamoDBAttribute.class) != null) {
			return method.getAnnotation(DynamoDBAttribute.class).attributeName();
		}
		if (method.getAnnotation(DynamoDBHashKey.class) != null) {
			return method.getAnnotation(DynamoDBHashKey.class).attributeName();
		}
		if (method.getAnnotation(DynamoDBRangeKey.class) != null) {
			return method.getAnnotation(DynamoDBRangeKey.class).attributeName();
		}
		if (method.getAnnotation(DynamoDBIndexRangeKey.class) != null) {
			return method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName();
		}
		if (method.getAnnotation(DynamoDBVersionAttribute.class) != null) {
			return method.getAnnotation(DynamoDBVersionAttribute.class).attributeName();
		}

		return null;

	}

	@Override
	public T getHashKeyPropotypeEntityForHashKey(Object hashKey) {

		Method hashKeySetterMethod = getHashKeySetterMethod();

		try {
			T entity = getJavaType().newInstance();
			ReflectionUtils.invokeMethod(hashKeySetterMethod, entity, hashKey);

			return entity;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isCompositeIdProperty(String propertyName) {
		return isPropertyAnnotatedWith(propertyName, Id.class);
	}

	@Override
	public DynamoDBMarshaller<?> getMarshallerForProperty(final String propertyName) {
		
		Method method = ReflectionUtils.findMethod(domainType, toMethodName(propertyName));
		if (method.getAnnotation(DynamoDBMarshalling.class) != null) {
			try {
				return method.getAnnotation(DynamoDBMarshalling.class).marshallerClass().newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);

			}
		}
		
		return null;
	}

}
