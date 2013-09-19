package org.socialsignin.spring.data.dynamodb.repository.support;

import java.io.Serializable;

public interface HashAndRangeKeyExtractor<ID extends Serializable,H> extends HashKeyExtractor<ID,H>{

	public Object getRangeKey(ID id);
	
}
