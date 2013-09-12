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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityInformationImpl<T, ID extends Serializable> extends GetterReflectionEntityInformation<T, ID>
		implements DynamoDBEntityInformation<T, ID> {

	private DynamoDBEntityMetadata<T,ID> metadata;

	public DynamoDBEntityInformationImpl(Class<T> domainClass, DynamoDBEntityMetadata<T,ID> metadata) {
		super(domainClass, DynamoDBHashKey.class);
		this.metadata = metadata;
	}

	@Override
	public boolean hasCompositeId() {
		return metadata.hasCompositeId();

	}
	
	@Override
	public boolean isNew(T entity) {
		// TODO ML Add version property check if appropriate
		return super.isNew(entity);
	}

	@Override
	public Object getHashKey(final ID id) {
		return id;
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
		return false;
	}

	@Override
	public  T getHashKeyPropotypeEntityForHashKey(Object hashKey) {
		return metadata.getHashKeyPropotypeEntityForHashKey(hashKey);
	}

	@Override
	public Object getRangeKey(ID id) {
		return null;
	}
	
	@Override
	public boolean isCompositeIdProperty(String propertyName) {
		return metadata.isCompositeIdProperty(propertyName);
	}

	@Override
	public DynamoDBMarshaller<?> getMarshallerForProperty(String propertyName) {
		return metadata.getMarshallerForProperty(propertyName);
	}

	

	
}
