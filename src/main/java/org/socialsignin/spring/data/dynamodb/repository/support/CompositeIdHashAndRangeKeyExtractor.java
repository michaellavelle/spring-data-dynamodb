package org.socialsignin.spring.data.dynamodb.repository.support;

import java.io.Serializable;

import org.springframework.util.ReflectionUtils;

public class CompositeIdHashAndRangeKeyExtractor<ID extends Serializable,H> implements HashAndRangeKeyExtractor<ID,H> {

	private DynamoDBHashAndRangeKeyMethodExtractor<ID> hashAndRangeKeyMethodExtractor;
	
	public CompositeIdHashAndRangeKeyExtractor(Class<ID> idClass)
	{
		this.hashAndRangeKeyMethodExtractor = new DynamoDBHashAndRangeKeyMethodExtractorImpl<ID>(idClass);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public H getHashKey(ID id) {

		return (H) ReflectionUtils.invokeMethod(hashAndRangeKeyMethodExtractor.getHashKeyMethod(), id);
	}
	
	@Override
	public Object getRangeKey(ID id) {
		return ReflectionUtils.invokeMethod(hashAndRangeKeyMethodExtractor.getRangeKeyMethod(), id);
	}

	
}
