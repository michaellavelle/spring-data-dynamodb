package org.socialsignin.spring.data.dynamodb.repository.support;

import java.io.Serializable;

import org.springframework.util.Assert;

public class HashKeyIsIdHashKeyExtractor<ID extends Serializable> implements HashKeyExtractor<ID,ID> {

	private Class<ID> idAndHashKeyType;
	
	public HashKeyIsIdHashKeyExtractor(Class<ID> idAndHashKeyType)
	{
		this.idAndHashKeyType = idAndHashKeyType;
	}
	
	@Override
	public ID getHashKey(ID id) {
		Assert.isAssignable(idAndHashKeyType, id.getClass(),"Expected ID type to be the same as the return type of the hash key method ( " + idAndHashKeyType + " ) : ");
		return id;
	}


}
