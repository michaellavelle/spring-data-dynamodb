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

import org.springframework.util.Assert;


/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityWithCompositeIdMetadataImpl<T, ID extends Serializable> extends
		DynamoDBEntityMetadataSupport<T,ID> implements DynamoDBEntityWithCompositeIdMetadata<T, ID> {

	public DynamoDBEntityWithCompositeIdMetadataImpl(Class<T> domainType) {
		super(domainType);
	}

	@Override
	public DynamoDBCompositeIdMetadata<ID> getCompositeIdMetadata(Class<ID> idClass) {
		return new DynamoDBCompositeIdMetadataImpl<ID>(idClass);
	}
	
	
	private String getPropertyNameForAccessorMethod(Method method)
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
	public String getRangeKeyPropertyName() {

		// Obtain hash and range key methods of current entity,
		// using the same extractor as we use for ids
		// TODO Rename/refactor DynamoDBCompositeIdMetadata so it is more generic
		DynamoDBCompositeIdMetadata<T> entityWithCompositeIdMetadata
		= new DynamoDBCompositeIdMetadataImpl<T>(getJavaType());
		return getPropertyNameForAccessorMethod(entityWithCompositeIdMetadata.getRangeKeyMethod());

	}

	@Override
	public String getHashKeyPropertyName() {

		// Obtain hash and range key methods of current entity,
		// using the same extractor as we use for ids
		// TODO Rename/refactor DynamoDBCompositeIdMetadata so it is more generic
		DynamoDBCompositeIdMetadata<T> entityWithCompositeIdMetadata
		= new DynamoDBCompositeIdMetadataImpl<T>(getJavaType());
		return getPropertyNameForAccessorMethod(entityWithCompositeIdMetadata.getHashKeyMethod());

	}


}
