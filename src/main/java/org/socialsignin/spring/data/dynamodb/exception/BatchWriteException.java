package org.socialsignin.spring.data.dynamodb.exception;

import org.springframework.dao.DataAccessException;

@SuppressWarnings("serial")
public class BatchWriteException extends DataAccessException {

	public BatchWriteException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
