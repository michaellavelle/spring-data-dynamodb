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

import org.springframework.util.ReflectionUtils;

/**
 * @author Michael Lavelle
 */
public class CompositeIdHashAndRangeKeyExtractor<ID extends Serializable, H> implements HashAndRangeKeyExtractor<ID, H> {

	private DynamoDBHashAndRangeKeyMethodExtractor<ID> hashAndRangeKeyMethodExtractor;

	public CompositeIdHashAndRangeKeyExtractor(Class<ID> idClass) {
		this.hashAndRangeKeyMethodExtractor = new DynamoDBHashAndRangeKeyMethodExtractorImpl<ID>(idClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public H getHashKey(ID id) {

		return (H) ReflectionUtils.invokeMethod(hashAndRangeKeyMethodExtractor.getHashKeyMethod(), id);
	}

	@Override
	public Object getRangeKey(ID id) {
		return ReflectionUtils.invokeMethod(hashAndRangeKeyMethodExtractor.getRangeKeyMethod(), id);
	}

}
