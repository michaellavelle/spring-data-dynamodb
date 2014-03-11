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
package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;

import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.query.QueryRequestMapper;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * @author Michael Lavelle
 */
public class PartTreeDynamoDBQuery<T, ID extends Serializable> extends AbstractDynamoDBQuery<T, ID> implements RepositoryQuery {

	private DynamoDBQueryMethod<T, ID> queryMethod;
	private final Parameters<?, ?> parameters;

	private QueryRequestMapper queryRequestMapper;

	private final PartTree tree;

	public PartTreeDynamoDBQuery(DynamoDBMapper dynamoDBMapper, DynamoDBQueryMethod<T, ID> method,
			QueryRequestMapper queryRequestMapper) {
		super(dynamoDBMapper, method);
		this.queryMethod = method;
		this.parameters = method.getParameters();
		this.tree = new PartTree(method.getName(), method.getEntityType());
		this.queryRequestMapper = queryRequestMapper;
	}

	public PartTree getTree() {
		return tree;
	}

	protected DynamoDBQueryCreator<T, ID> createQueryCreator(ParametersParameterAccessor accessor) {
		return new DynamoDBQueryCreator<T, ID>(tree, accessor, queryMethod.getEntityInformation(), dynamoDBMapper,
				queryRequestMapper);
	}
	
	protected DynamoDBCountQueryCreator<T, ID> createCountQueryCreator(ParametersParameterAccessor accessor,boolean pageQuery) {
		return new DynamoDBCountQueryCreator<T, ID>(tree, accessor, queryMethod.getEntityInformation(), dynamoDBMapper,
				queryRequestMapper,pageQuery);
	}

	@Override
	public Query<T> doCreateQuery(Object[] values) {

		ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
		DynamoDBQueryCreator<T, ID> queryCreator = createQueryCreator(accessor);
		return queryCreator.createQuery();

	}
	
	@Override
	public Query<Long> doCreateCountQuery(Object[] values,boolean pageQuery) {

		ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
		DynamoDBCountQueryCreator<T, ID> queryCreator = createCountQueryCreator(accessor,pageQuery);
		return queryCreator.createQuery();

	}

	@Override
	protected boolean isCountQuery() {
		return tree.isCountProjection();
	}

}
