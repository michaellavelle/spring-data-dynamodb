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
import java.util.Iterator;
import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBHashAndRangeKey;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

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
			return new PagedExecution(method.getParameters());
		
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

	/**
	 * Executes the {@link AbstractStringBasedJpaQuery} to return a
	 * {@link org.springframework.data.domain.Page} of entities.
	 */
	class PagedExecution extends AbstractQueryExecution {

		private final Parameters<?, ?> parameters;

		public PagedExecution(Parameters<?, ?> parameters) {

			this.parameters = parameters;
		}
		
		private int scanThroughResults(Iterator<T> iterator,int resultsToScan)
		{
			int processed = 0;
			while (iterator.hasNext() && processed < resultsToScan)
			{
				iterator.next();
				processed++;
			}
			return processed;
		}
		
		private List<T> readPageOfResults(Iterator<T> iterator,int pageSize)
		{
			int processed = 0;
			List<T> resultsPage = new ArrayList<T>();
			while (iterator.hasNext() && processed < pageSize)
			{
				resultsPage.add(iterator.next());
				processed++;
			}
			return resultsPage;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object execute(AbstractDynamoDBQuery<T, ID> query, Object[] values) {

			
			DynamoDBCriteria<T, ID> criteria = query.doCreateDynamoDBCriteria(values);
			DynamoDBHashAndRangeKey hashAndRangeKey = criteria.buildLoadCriteria();
			ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);

			Pageable pageable = accessor.getPageable();

			if (hashAndRangeKey != null) {

				if (hashAndRangeKey.getRangeKey() == null) {

					Object o = dynamoDBMapper.load(query.getQueryMethod().getReturnedObjectType(),
							hashAndRangeKey.getHashKey());
					if (o != null) {
						return new PageImpl<Object>(Arrays.asList(o));
					} else {
						return new PageImpl<Object>(new ArrayList<Object>());
					}
				} else {

					Object o = dynamoDBMapper.load(query.getQueryMethod().getReturnedObjectType(),
							hashAndRangeKey.getHashKey(), hashAndRangeKey.getRangeKey());
					if (o != null) {

						return new PageImpl<Object>(Arrays.asList(o));
					} else {
						return new PageImpl<Object>(new ArrayList<Object>());
					}
				}

			}

			DynamoDBQueryExpression<T> queryExpression = criteria.buildQueryExpression();
			if (queryExpression != null) {

				// Query to the end of the page after the requested page
				int resultsLimit = pageable.getOffset() +  (2 * pageable.getPageSize());
				queryExpression.setLimit(resultsLimit);

				PaginatedQueryList<T> paginatedQueryList =  dynamoDBMapper.query(query.getQueryMethod().getEntityType(), queryExpression);
				Iterator<T> iterator = paginatedQueryList.iterator();
				int processedCount = 0;
				if (pageable.getOffset() > 0)
				{
					processedCount = scanThroughResults(iterator,pageable.getOffset());
					if (processedCount < pageable.getOffset()) return new PageImpl<T>(new ArrayList<T>());
				}
				// Scan ahead to retrieve the next page count
				List<T> results = readPageOfResults(iterator,pageable.getPageSize());
				int nextPageItemCount = scanThroughResults(iterator,pageable.getPageSize());
				boolean hasMoreResults = nextPageItemCount > 0;
				int totalProcessed = processedCount + results.size();
				// Set total count to be the number already returned, or the number returned added to the count of the next page
				// This allows paging to determine next/page prev page correctly, even though we are unable to return
				// the actual count of total results due to the way DynamoDB scans results
				return new PageImpl<T>(results,pageable,hasMoreResults ? (totalProcessed + nextPageItemCount) : totalProcessed);
			}

			DynamoDBScanExpression scanExpression = criteria.buildScanExpression();
			if (scanExpression != null) {
				PaginatedScanList<T> paginatedScanList = (PaginatedScanList<T> )dynamoDBMapper.scan(query.getQueryMethod().getReturnedObjectType(), scanExpression);
			
				Iterator<T> iterator = paginatedScanList.iterator();
				int processedCount = 0;
				if (pageable.getOffset() > 0)
				{
					processedCount = scanThroughResults(iterator,pageable.getOffset());
					if (processedCount < pageable.getOffset()) return new PageImpl<T>(new ArrayList<T>());
				}
				List<T> results = readPageOfResults(iterator,pageable.getPageSize());
				// Scan ahead to retrieve the next page count
				int nextPageItemCount = scanThroughResults(iterator,pageable.getPageSize());
				boolean hasMoreResults = nextPageItemCount > 0;
				int totalProcessed = processedCount + results.size();
				// Set total count to be the number already returned, or the number returned added to the count of the next page
				// This allows paging to determine next/page prev page correctly, even though we are unable to return
				// the actual count of total results due to the way DynamoDB scans results
				return new PageImpl<T>(results,pageable,hasMoreResults ? (totalProcessed + nextPageItemCount) : totalProcessed);
			
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
