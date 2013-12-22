/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.query;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * 
 * @author Michael Lavelle
 */
public abstract class AbstractMultipleEntityQuery<T> extends AbstractQuery<T> implements Query<T> {

	public AbstractMultipleEntityQuery(DynamoDBMapper dynamoDBMapper, Class<T> clazz) {
		super(dynamoDBMapper, clazz);
	}

	@Override
	public T getSingleResult() {
		List<T> results = getResultList();
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException("result returns more than one elements", 1, results.size());
		}
		if (results.size() == 0) {
			throw new EmptyResultDataAccessException("No results found", 1);
		}
		return results.get(0);
	}
}
