package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

public class DynamoDBQueryCreator<T,ID extends Serializable> extends AbstractDynamoDBQueryCreator<T, ID,T> {

	private final Pageable pageable;

	public DynamoDBQueryCreator(PartTree tree,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations) {
		super(tree, entityMetadata, dynamoDBOperations);
		pageable = null;
	}

	public DynamoDBQueryCreator(PartTree tree,
			ParameterAccessor parameterAccessor,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations) {
		super(tree, parameterAccessor, entityMetadata, dynamoDBOperations);
		pageable = parameterAccessor.getPageable();
	}

	@Override
	protected Query<T> complete(DynamoDBQueryCriteria<T, ID> criteria, Sort sort) {
		if (sort != null) {
			criteria.withSort(sort);
		}
		if (pageable != null) {
			criteria.withPageable(pageable);
		}

		return criteria.buildQuery(dynamoDBOperations);

	}

}
