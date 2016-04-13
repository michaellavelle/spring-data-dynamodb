package org.socialsignin.spring.data.dynamodb.marshaller;

import java.time.Instant;

import org.springframework.util.StringUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class Instant2EpocheDynamoDBMarshaller implements DynamoDBMarshaller<Instant> {

	@Override
	public String marshall(Instant getterReturnResult) {
		if (getterReturnResult == null) {
			return null;
		} else {
			return Long.toString(getterReturnResult.toEpochMilli());
		}
	}

	@Override
	public Instant unmarshall(Class<Instant> clazz, String obj) {
		if (StringUtils.isEmpty(obj)) {
			return null;
		} else {
			return Instant.ofEpochMilli(Long.parseLong(obj));
		}
	}

}
