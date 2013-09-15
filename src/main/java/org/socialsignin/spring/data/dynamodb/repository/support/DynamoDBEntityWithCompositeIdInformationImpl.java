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
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;
import org.springframework.util.ReflectionUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityWithCompositeIdInformationImpl<T, ID extends Serializable> extends
		ReflectionEntityInformation<T, ID> implements DynamoDBEntityWithCompositeIdInformation<T,ID> {

	private DynamoDBEntityWithCompositeIdMetadata<T, ID> metadata;

	public DynamoDBEntityWithCompositeIdInformationImpl(Class<T> domainClass,
			DynamoDBEntityWithCompositeIdMetadata<T, ID> metadata) {
		super(domainClass, Id.class);
		this.metadata = metadata;
	}

	@Override
	public boolean hasCompositeId() {
		return metadata.hasCompositeId();
	}

	@Override
	public Object getHashKey(final ID id) {
		return ReflectionUtils.invokeMethod(metadata.getCompositeIdMetadata(getIdType()).getHashKeyMethod(), id);
	}

	@Override
	public Object getRangeKey(final Serializable id) {
		return ReflectionUtils.invokeMethod(metadata.getCompositeIdMetadata(getIdType()).getRangeKeyMethod(), id);
	}

	
	@Override
	public String getOverriddenAttributeName(String attributeName) {
		return metadata.getOverriddenAttributeName(attributeName);
	}


	@Override
	public boolean isHashKeyProperty(String propertyName) {
			return metadata.isHashKeyProperty(propertyName);
	}

	@Override
	public boolean isRangeKeyProperty(String propertyName) {
		return metadata.isRangeKeyProperty(propertyName);

	}

	@Override
	public T getHashKeyPropotypeEntityForHashKey(Object hashKey) {
		return metadata.getHashKeyPropotypeEntityForHashKey(hashKey);
	}
	

	@Override
	public boolean isCompositeIdProperty(String propertyName) {
		return metadata.isCompositeIdProperty(propertyName);
	}

	@Override
	public DynamoDBCompositeIdMetadata<ID> getCompositeIdMetadata(
			Class<ID> idClass) {
		return metadata.getCompositeIdMetadata(idClass);
	}

	@Override
	public String getRangeKeyPropertyName() {
		return metadata.getRangeKeyPropertyName();
	}

	@Override
	public String getHashKeyPropertyName() {
		return metadata.getHashKeyPropertyName();
	}

	@Override
	public DynamoDBMarshaller<?> getMarshallerForProperty(String propertyName) {
		return metadata.getMarshallerForProperty(propertyName);
	}

	@Override
	public Set<String> getIndexRangeKeyPropertyNames() {
		return metadata.getIndexRangeKeyPropertyNames();
	}


	
	
	

}
