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

import com.amazonaws.services.dynamodbv2.model.QueryRequest;

public class MultipleEntityQueryRequestQuery<T> extends AbstractMultipleEntityQuery<T> {

	private QueryRequestMapper queryRequestMapper;
	private QueryRequest queryRequest;
	
	public MultipleEntityQueryRequestQuery(QueryRequestMapper queryRequestMapper,Class<T> clazz,QueryRequest queryRequest) {
		super(null, clazz);
		this.queryRequest = queryRequest;
		this.queryRequestMapper = queryRequestMapper;
	}
	
	@Override
	public List<T> getResultList() {

		return queryRequestMapper.query(clazz, queryRequest);		
	}

}
