package org.socialsignin.spring.data.dynamodb.repository.query;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class CaseChangingMarshaller implements DynamoDBMarshaller<String> {

	@Override
	public String marshall(String getterReturnResult) {
		return getterReturnResult == null ? null : getterReturnResult.toLowerCase();
	}

	@Override
	public String unmarshall(Class<String> clazz, String obj) {
		return obj == null ? null : obj.toUpperCase();
	}

}
