package org.socialsignin.spring.data.dynamodb.query;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public abstract class AbstractMultipleEntityQuery<T> extends AbstractQuery<T> implements Query<T> {

	
	public AbstractMultipleEntityQuery(DynamoDBMapper dynamoDBMapper,Class<T> clazz) {
		super(dynamoDBMapper,clazz);
	}
	
	@Override
	public T getSingleResult() {
		List<T> results = getResultList();
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
