package org.socialsignin.spring.data.dynamodb.repository.query;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

public class DynamoDBCountQueryCreator<T ,ID> extends AbstractDynamoDBQueryCreator<T, ID, Long> {

	private boolean pageQuery;
	
	public DynamoDBCountQueryCreator(PartTree tree,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations,boolean pageQuery) {
		super(tree, entityMetadata, dynamoDBOperations);
		this.pageQuery = pageQuery;
	}

	public DynamoDBCountQueryCreator(PartTree tree,
			ParameterAccessor parameterAccessor,
			DynamoDBEntityInformation<T, ID> entityMetadata,
			DynamoDBOperations dynamoDBOperations,boolean pageQuery) {
		super(tree, parameterAccessor, entityMetadata, dynamoDBOperations);
		this.pageQuery = pageQuery;

	}
	
	@Override
	protected Query<Long> complete(DynamoDBQueryCriteria<T, ID> criteria, Sort sort) {
	
		return criteria.buildCountQuery(dynamoDBOperations,pageQuery);

	}

}
