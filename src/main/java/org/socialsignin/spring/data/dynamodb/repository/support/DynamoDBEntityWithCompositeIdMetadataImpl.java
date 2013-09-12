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

	@Override
	public String getRangeKeyPropertyName() {

		// Obtain hash and range key methods of current entity,
		// using the same extractor as we use for ids
		// TODO Rename/refactor DynamoDBCompositeIdMetadata so it is more generic
		DynamoDBCompositeIdMetadata<T> entityWithCompositeIdMetadata
		= new DynamoDBCompositeIdMetadataImpl<T>(getJavaType());
		String rangeKeyMethodName = entityWithCompositeIdMetadata.getRangeKeyMethod().getName();
		String rangeKeyFieldName = rangeKeyMethodName.substring(3);
		String firstLetter = rangeKeyFieldName.substring(0,1);
		String remainder = rangeKeyFieldName.substring(1);
		String rangeKeyProperty =  firstLetter.toLowerCase() + remainder;
		return rangeKeyProperty;

	}

	@Override
	public String getHashKeyPropertyName() {

		// Obtain hash and range key methods of current entity,
		// using the same extractor as we use for ids
		// TODO Rename/refactor DynamoDBCompositeIdMetadata so it is more generic
		DynamoDBCompositeIdMetadata<T> entityWithCompositeIdMetadata
		= new DynamoDBCompositeIdMetadataImpl<T>(getJavaType());
		String hashKeyMethodName = entityWithCompositeIdMetadata.getHashKeyMethod().getName();
		String hashKeyFieldName = hashKeyMethodName.substring(3);
		String firstLetter = hashKeyFieldName.substring(0,1);
		String remainder = hashKeyFieldName.substring(1);
		String hashKeyProperty =  firstLetter.toLowerCase() + remainder;
		return hashKeyProperty;
	}


}
