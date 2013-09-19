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
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;


/**
 * @author Michael Lavelle
 */
public class DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl<T, ID extends Serializable> extends
		DynamoDBEntityMetadataSupport<T,ID> implements DynamoDBHashAndRangeKeyExtractingEntityMetadata<T, ID> {

	private DynamoDBHashAndRangeKeyMethodExtractor<T> hashAndRangeKeyMethodExtractor;
	
	public DynamoDBHashAndRangeKeyExtractingEntityMetadataImpl(Class<T> domainType) {
		super(domainType);
		this.hashAndRangeKeyMethodExtractor = new DynamoDBHashAndRangeKeyMethodExtractorImpl<T>(getJavaType());
	}

	@Override
	public <H> HashAndRangeKeyExtractor<ID,H> getHashAndRangeKeyExtractor(Class<ID> idClass) {
		return new CompositeIdHashAndRangeKeyExtractor<ID,H>(idClass);
	}
	
	@Override
	public String getRangeKeyPropertyName() {
		return getPropertyNameForAccessorMethod(hashAndRangeKeyMethodExtractor.getRangeKeyMethod());
	}

	@Override
	public Set<String> getIndexRangeKeyPropertyNames() {
		final Set<String> propertyNames = new HashSet<String>();
		ReflectionUtils.doWithMethods(getJavaType(), new MethodCallback() {
			public void doWith(Method method) {
				if (method.getAnnotation(DynamoDBIndexRangeKey.class) != null) {
					propertyNames.add(getPropertyNameForAccessorMethod(method));
				}
			}
		});
		return propertyNames;
	}

	@Override
	public boolean isCompositeHashAndRangeKeyProperty(String propertyName) {
		return isFieldAnnotatedWith(propertyName,Id.class);
	}

}
