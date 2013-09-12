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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBHashAndRangeKey;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.repository.query.RepositoryQuery;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

/**
 * @author Michael Lavelle
 */
public abstract class AbstractDynamoDBQuery<T, ID extends Serializable> implements RepositoryQuery {

	private final DynamoDBMapper dynamoDBMapper;
	private final DynamoDBQueryMethod<T, ID> method;

	public AbstractDynamoDBQuery(DynamoDBMapper dynamoDBMapper, DynamoDBQueryMethod<T, ID> method) {
		this.dynamoDBMapper = dynamoDBMapper;
		this.method = method;
	}

	protected QueryExecution<T, ID> getExecution() {

		if (method.isCollectionQuery()) {
			return new CollectionExecution();
		} else if (method.isPageQuery()) {
			throw new UnsupportedOperationException("Page queries not yet supported");
		} else if (method.isModifyingQuery()) {
			throw new UnsupportedOperationException("Modifying queries not yet supported");
		} else {
			return new SingleEntityExecution();
		}
	}

	protected abstract DynamoDBCriteria<T, ID> doCreateDynamoDBCriteria(Object[] values);

	private interface QueryExecution<T, ID extends Serializable> {
		public Object execute(AbstractDynamoDBQuery<T, ID> query, Object[] values);
	}

	/**
	 * Base class for query execution implementing {@link QueryExecution}
	 * 
	 * @author Michael Lavelle
	 */
	abstract class AbstractQueryExecution implements QueryExecution<T, ID> {

	}

	class CollectionExecution extends AbstractQueryExecution {

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> query, Object[] values) {
			DynamoDBCriteria<T, ID> criteria = query.doCreateDynamoDBCriteria(values);
			DynamoDBHashAndRangeKey hashAndRangeKey = criteria.buildLoadCriteria();
			if (hashAndRangeKey != null) {

				if (hashAndRangeKey.getRangeKey() == null) {

					Object o = dynamoDBMapper.load(query.getQueryMethod().getReturnedObjectType(),
							hashAndRangeKey.getHashKey());
					if (o != null) {
						return Arrays.asList(o);
					} else {
						return new ArrayList<T>();
					}
				} else {

					Object o = dynamoDBMapper.load(query.getQueryMethod().getReturnedObjectType(),
							hashAndRangeKey.getHashKey(), hashAndRangeKey.getRangeKey());
					if (o != null) {

						return Arrays.asList(o);
					} else {
						return new ArrayList<T>();
					}
				}

			}

			DynamoDBQueryExpression<T> queryExpression = criteria.buildQueryExpression();
			if (queryExpression != null) {

				List<T> results = dynamoDBMapper.query(query.getQueryMethod().getEntityType(), queryExpression);
				return results;
			}

			DynamoDBScanExpression scanExpression = criteria.buildScanExpression();
			if (scanExpression != null) {
				return dynamoDBMapper.scan(query.getQueryMethod().getReturnedObjectType(), scanExpression);
			} else {
				return null;
			}
		}

	}

	class SingleEntityExecution extends AbstractQueryExecution {

		@Override
		public Object execute(AbstractDynamoDBQuery<T, ID> query, Object[] values) {

			DynamoDBCriteria<T, ID> criteria = query.doCreateDynamoDBCriteria(values);

			DynamoDBHashAndRangeKey hashAndRangeKey = criteria.buildLoadCriteria();
			if (hashAndRangeKey != null) {
				if (hashAndRangeKey.getRangeKey() == null) {

					Object o = dynamoDBMapper.load(query.getQueryMethod().getReturnedObjectType(),
							hashAndRangeKey.getHashKey());
					return o;
				} else {

					Object o = dynamoDBMapper.load(query.getQueryMethod().getReturnedObjectType(),
							hashAndRangeKey.getHashKey(), hashAndRangeKey.getRangeKey());

					return o;
				}
			} else {
				DynamoDBQueryExpression<T> queryExpression = criteria.buildQueryExpression();
				List<?> results = null;
				if (queryExpression != null) {
					results = dynamoDBMapper.query(query.getQueryMethod().getEntityType(), queryExpression);
				} else {

					DynamoDBScanExpression scanExpression = criteria.buildScanExpression();

					results = dynamoDBMapper.scan(query.getQueryMethod().getReturnedObjectType(), scanExpression);
				}

				if (results.size() > 1) {
					throw new IncorrectResultSizeDataAccessException("result returns more than one elements", 1,
							results.size());
				}
				if (results.size() == 0) {
					throw new EmptyResultDataAccessException("No results found", 1);
				}

				return results.get(0);
			}

		}

	}
	/**
	 * @param execution
	 * @param values
	 * @return
	 */
	private Object doExecute(QueryExecution<T, ID> execution, Object[] values) {

		return execution.execute(this, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.query.RepositoryQuery#execute(java
	 * .lang.Object[])
	 */
	public Object execute(Object[] parameters) {

		return doExecute(getExecution(), parameters);
	}

	@Override
	public DynamoDBQueryMethod<T, ID> getQueryMethod() {
		return this.method;
	}

}
