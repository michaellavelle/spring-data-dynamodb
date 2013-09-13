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
import java.util.Iterator;

import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.PartTree;

import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
/**
 * @author Michael Lavelle
 */
public class DynamoDBQueryCreator<T,ID extends Serializable> extends AbstractQueryCreator<DynamoDBCriteria<T,ID>, DynamoDBCriteria<T,ID>> {

	private DynamoDBEntityInformation<T,ID> entityMetadata;
	
	public DynamoDBQueryCreator(PartTree tree,DynamoDBEntityInformation<T,ID> entityMetadata) {
		super(tree);
		this.entityMetadata = entityMetadata;
	}
	
	public DynamoDBQueryCreator(PartTree tree,ParameterAccessor parameterAccessor,DynamoDBEntityInformation<T,ID> entityMetadata) {
		super(tree,parameterAccessor);
		this.entityMetadata = entityMetadata;
	}
	
	@Override
	protected DynamoDBCriteria<T,ID> create(Part part, Iterator<Object> iterator) {
		
		DynamoDBCriteria<T,ID> criteria = new DynamoDBCriteria<T,ID>(entityMetadata);
		return addCriteria(criteria,part,iterator);
	}
	

	
	protected DynamoDBCriteria<T,ID> addCriteria(DynamoDBCriteria<T,ID> criteria,Part part, Iterator<Object> iterator)
	{
		if (part.shouldIgnoreCase().equals(IgnoreCaseType.ALWAYS))
			throw new UnsupportedOperationException("Case insensitivity not supported");
		
		switch (part.getType()) {
		case TRUE:
			return criteria.withPropertyCriteria(part.getProperty().getSegment(), ComparisonOperator.EQ,Boolean.TRUE);
		case FALSE:
			return criteria.withPropertyCriteria(part.getProperty().getSegment(), ComparisonOperator.EQ,Boolean.FALSE);
		case SIMPLE_PROPERTY:
			return criteria.withPropertyEquals(part.getProperty().getSegment(), iterator.next());
		case NEGATING_SIMPLE_PROPERTY:
			//return builder.notEqual(upperIfIgnoreCase(path), upperIfIgnoreCase(provider.next(part).getExpression()));
			return criteria.withPropertyCriteria(part.getProperty().getSegment(), ComparisonOperator.NE,iterator.next());
		default:
			throw new IllegalArgumentException("Unsupported keyword " + part.getType());
		}
		
	}
	
	@Override
	protected DynamoDBCriteria<T,ID> and(Part part, DynamoDBCriteria<T,ID> base,
			Iterator<Object> iterator) {		
		return addCriteria(base,part,iterator);
		
	}

	@Override
	protected DynamoDBCriteria<T,ID> or(DynamoDBCriteria<T,ID> base,
			DynamoDBCriteria<T,ID> criteria) {
		throw new UnsupportedOperationException("Or queries not yet supported");
	}

	@Override
	protected DynamoDBCriteria<T,ID> complete(DynamoDBCriteria<T,ID> criteria, Sort sort) {
		if (sort != null)
		{
			return criteria.withSort(sort);
		}
		
		return criteria;
	}



}
