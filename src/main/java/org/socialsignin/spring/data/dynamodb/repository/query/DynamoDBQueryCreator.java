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
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.ClassUtils;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBIdIsHashAndRangeKeyEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
/**
 * @author Michael Lavelle
 */
public class DynamoDBQueryCreator<T,ID extends Serializable> extends AbstractQueryCreator<Query<T>, DynamoDBQueryCriteria<T,ID>> {

	private DynamoDBEntityInformation<T,ID> entityMetadata;
	private DynamoDBMapper dynamoDBMapper;
	
	public DynamoDBQueryCreator(PartTree tree,DynamoDBEntityInformation<T,ID> entityMetadata,DynamoDBMapper dynamoDBMapper) {
		super(tree);
		this.entityMetadata = entityMetadata;
		this.dynamoDBMapper = dynamoDBMapper;
	}
	
	public DynamoDBQueryCreator(PartTree tree,ParameterAccessor parameterAccessor,DynamoDBEntityInformation<T,ID> entityMetadata,DynamoDBMapper dynamoDBMapper) {
		super(tree,parameterAccessor);
		this.entityMetadata = entityMetadata;
		this.dynamoDBMapper = dynamoDBMapper;

	}
	
	@Override
	protected DynamoDBQueryCriteria<T,ID> create(Part part, Iterator<Object> iterator) {
		
		DynamoDBQueryCriteria<T,ID> criteria = entityMetadata.isRangeKeyAware() ? new DynamoDBEntityWithHashAndRangeKeyCriteria<T,ID>((DynamoDBIdIsHashAndRangeKeyEntityInformation<T,ID>)entityMetadata) : 
			new DynamoDBEntityWithHashKeyOnlyCriteria<T,ID>(entityMetadata);
		return addCriteria(criteria,part,iterator);
	}
	

	
	protected DynamoDBQueryCriteria<T,ID> addCriteria(DynamoDBQueryCriteria<T,ID> criteria,Part part, Iterator<Object> iterator)
	{
		if (part.shouldIgnoreCase().equals(IgnoreCaseType.ALWAYS))
			throw new UnsupportedOperationException("Case insensitivity not supported");
		
		Class<?> propertyType = part.getProperty().getType();
		
		switch (part.getType()) {
		case IN:
		Object in = iterator.next();

		boolean isIterable = ClassUtils.isAssignable(in.getClass(), Iterable.class);
		boolean isArray = ObjectUtils.isArray(in);
		Assert.isTrue(isIterable || isArray,"In criteria can only operate with Iterable or Array parameters");
			Iterable<?> iterable = isIterable ? ((Iterable<?>)in) : Arrays.asList(ObjectUtils.toObjectArray(in));
			return criteria.withPropertyIn(part.getProperty().getSegment(), iterable,propertyType);
		case AFTER:
			// TODO Do we want to check that value is a DynamoDB date here - ie. a Date object or a String?
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.GT,iterator.next(),propertyType);
		case BEFORE:
			// TODO Do we want to check that value is a DynamoDB date here - ie. a Date object or a String?
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.LT,iterator.next(),propertyType);
		case GREATER_THAN:
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.GT,iterator.next(),propertyType);
		case LESS_THAN:
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.LT,iterator.next(),propertyType);
		case GREATER_THAN_EQUAL:
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.GE,iterator.next(),propertyType);
		case LESS_THAN_EQUAL:
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.LE,iterator.next(),propertyType);
		case IS_NULL:
			return criteria.withNoValuedCriteria(part.getProperty().getSegment(), ComparisonOperator.NULL);
		case IS_NOT_NULL:
			return criteria.withNoValuedCriteria(part.getProperty().getSegment(), ComparisonOperator.NOT_NULL);
		case TRUE:
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.EQ,Boolean.TRUE,propertyType);
		case FALSE:
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.EQ,Boolean.FALSE,propertyType);
		case SIMPLE_PROPERTY:
			return criteria.withPropertyEquals(part.getProperty().getSegment(), iterator.next(),propertyType);	
		case NEGATING_SIMPLE_PROPERTY:
			//return builder.notEqual(upperIfIgnoreCase(path), upperIfIgnoreCase(provider.next(part).getExpression()));
			return criteria.withSingleValueCriteria(part.getProperty().getSegment(), ComparisonOperator.NE,iterator.next(),propertyType);
		default:
			throw new IllegalArgumentException("Unsupported keyword " + part.getType());
		}
		
	}
	
	@Override
	protected DynamoDBQueryCriteria<T,ID> and(Part part, DynamoDBQueryCriteria<T,ID> base,
			Iterator<Object> iterator) {		
		return addCriteria(base,part,iterator);
		
	}

	@Override
	protected DynamoDBQueryCriteria<T,ID> or(DynamoDBQueryCriteria<T,ID> base,
			DynamoDBQueryCriteria<T,ID> criteria) {
		throw new UnsupportedOperationException("Or queries not yet supported");
	}

	@Override
	protected Query<T> complete(DynamoDBQueryCriteria<T,ID> criteria, Sort sort) {
		if (sort != null)
		{
			criteria.withSort(sort);
		}
		
		return criteria.buildQuery(dynamoDBMapper);
		
	}



}
