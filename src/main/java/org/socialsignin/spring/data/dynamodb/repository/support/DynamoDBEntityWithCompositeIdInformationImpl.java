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

import org.springframework.data.annotation.Id;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;
import org.springframework.util.ReflectionUtils;

/**
 * @author Michael Lavelle
 */
public class DynamoDBEntityWithCompositeIdInformationImpl<T, ID extends Serializable> extends
		ReflectionEntityInformation<T, ID> implements DynamoDBEntityInformation<T, ID> {

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

}
