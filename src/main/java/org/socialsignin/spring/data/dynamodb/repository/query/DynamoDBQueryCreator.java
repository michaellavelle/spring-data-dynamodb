package org.socialsignin.spring.data.dynamodb.repository.query;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

public class DynamoDBQueryCreator<T,ID> extends AbstractDynamoDBQueryCreator<T, ID,T> {

	public DynamoDBQueryCreator(PartTree tree,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations) {
		super(tree, entityMetadata, dynamoDBOperations);
	}

	public DynamoDBQueryCreator(PartTree tree,
			ParameterAccessor parameterAccessor,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations) {
		super(tree, parameterAccessor, entityMetadata, dynamoDBOperations);
	}
	
	@Override
	protected Query<T> complete(DynamoDBQueryCriteria<T, ID> criteria, Sort sort) {
		if (sort != null) {
			criteria.withSort(sort);
		}

		return criteria.buildQuery(dynamoDBOperations);

	}

}
