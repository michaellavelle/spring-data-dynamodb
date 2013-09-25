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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
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
public class DynamoDBEntityMetadataSupport<T, ID extends Serializable> implements DynamoDBHashKeyExtractingEntityMetadata<T> {

	private final Class<T> domainType; 
	private boolean hasRangeKey;
	private String hashKeyPropertyName;

	

	/**
	 * Creates a new {@link DefaultJpaEntityMetadata} for the given domain type.
	 * 
	 * @param domainType
	 *            must not be {@literal null}.
	 */
	public DynamoDBEntityMetadataSupport(final Class<T> domainType) {

		Assert.notNull(domainType, "Domain type must not be null!");
		this.domainType = domainType;
		ReflectionUtils.doWithMethods(domainType, new MethodCallback() {
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBHashKey.class) != null) {
					hashKeyPropertyName = getPropertyNameForAccessorMethod(method);
				}
				if (method.getAnnotation(DynamoDBRangeKey.class) != null) {
					hasRangeKey = true;
				}
			}});
		Assert.notNull(hashKeyPropertyName, "Unable to find hash key getter method on " + domainType + "!");
	}

	
	public DynamoDBEntityInformation<T, ID> getEntityInformation() {

		if (hasRangeKey) {
			DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl<T, ID> metadata = new DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl<T, ID>(
					domainType);
			return new DynamoDBIdIsHashAndRangeKeyEntityInformationImpl<T,ID>(domainType, metadata);
		} else {
			return new DynamoDBIdIsHashKeyEntityInformationImpl<T, ID>(domainType, this);
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
		return hashKeyPropertyName.equals(propertyName);
	}

	
	protected boolean isFieldAnnotatedWith(final String propertyName, final Class<? extends Annotation> annotation) {
		
		Field field = findField(propertyName);
		return field != null && field.getAnnotation(annotation) != null;
	}

	private String toGetMethodName(String propertyName) {
		String methodName = propertyName.substring(0, 1).toUpperCase();
		if (propertyName.length() > 1) {
			methodName = methodName + propertyName.substring(1);
		}
		return "get" + methodName;
	}
	
	protected String toSetterMethodNameFromAccessorMethod(Method method) {
		String accessorMethodName = method.getName();
		if (accessorMethodName.startsWith("get"))
		{
			return "set" + accessorMethodName.substring(3);
		}
		else if (accessorMethodName.startsWith("is"))
		{
			return "is" + accessorMethodName.substring(2);
		}
		return null;
	}
	
	private String toIsMethodName(String propertyName) {
		String methodName = propertyName.substring(0, 1).toUpperCase();
		if (propertyName.length() > 1) {
			methodName = methodName + propertyName.substring(1);
		}
		return "is" + methodName;
	}
	
	private Method findMethod(String propertyName)
	{
		Method method = ReflectionUtils.findMethod(domainType, toGetMethodName(propertyName));
		if (method == null)
		{
			method = ReflectionUtils.findMethod(domainType, toIsMethodName(propertyName));
		}
		return method;

	}
	
	private Field findField(String propertyName)
	{
		return ReflectionUtils.findField(domainType, propertyName);
	}

	
	public String getOverriddenAttributeName(Method method) {

		if (method != null)
		{
			if (method.getAnnotation(DynamoDBAttribute.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBAttribute.class).attributeName())) {
				return method.getAnnotation(DynamoDBAttribute.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBHashKey.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBHashKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBHashKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBRangeKey.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBRangeKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBRangeKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBIndexRangeKey.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBVersionAttribute.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBVersionAttribute.class).attributeName())) {
				return method.getAnnotation(DynamoDBVersionAttribute.class).attributeName();
			}
		}
		return null;

	}

	
	
	@Override
	public String getOverriddenAttributeName(final String propertyName) {

		Method method = findMethod(propertyName);
		if (method != null)
		{
			if (method.getAnnotation(DynamoDBAttribute.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBAttribute.class).attributeName())) {
				return method.getAnnotation(DynamoDBAttribute.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBHashKey.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBHashKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBHashKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBRangeKey.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBRangeKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBRangeKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBIndexRangeKey.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName())) {
				return method.getAnnotation(DynamoDBIndexRangeKey.class).attributeName();
			}
			if (method.getAnnotation(DynamoDBVersionAttribute.class) != null && StringUtils.isNotEmpty(method.getAnnotation(DynamoDBVersionAttribute.class).attributeName())) {
				return method.getAnnotation(DynamoDBVersionAttribute.class).attributeName();
			}
		}
		return null;

	}

	

	

	@Override
	public DynamoDBMarshaller<?> getMarshallerForProperty(final String propertyName) {
		
		Method method = findMethod(propertyName);
		if (method != null && method.getAnnotation(DynamoDBMarshalling.class) != null) {
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
	
	protected String getPropertyNameForAccessorMethod(Method method)
	{
		String methodName = method.getName();
		String propertyName = null;
		if (methodName.startsWith("get"))
		{
			propertyName = methodName.substring(3);
		}
		else if (methodName.startsWith("is"))
		{
			propertyName = methodName.substring(2);
		}
		Assert.notNull(propertyName,"Hash or range key annotated accessor methods must start with 'get' or 'is'");
		
		String firstLetter = propertyName.substring(0,1);
		String remainder = propertyName.substring(1);
		return firstLetter.toLowerCase() + remainder;
	}


	@Override
	public String getHashKeyPropertyName() {
		return hashKeyPropertyName;
	}

	

	

}
