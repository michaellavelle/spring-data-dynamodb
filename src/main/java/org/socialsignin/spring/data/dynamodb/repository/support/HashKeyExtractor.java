package org.socialsignin.spring.data.dynamodb.repository.support;

import java.io.Serializable;

public interface HashKeyExtractor<ID extends Serializable,H> {

	public H getHashKey(ID id);
}
