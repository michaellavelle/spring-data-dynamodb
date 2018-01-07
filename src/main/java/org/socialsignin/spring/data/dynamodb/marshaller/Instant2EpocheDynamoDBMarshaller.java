package org.socialsignin.spring.data.dynamodb.marshaller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.springframework.util.StringUtils;

import java.time.Instant;

public class Instant2EpocheDynamoDBMarshaller implements DynamoDBTypeConverter<String, Instant>, DynamoDBMarshaller<Instant> {

	@Override
	public String convert(Instant object) {
		return marshall(object);
	}

	@Override
	public String marshall(Instant getterReturnResult) {
		if (getterReturnResult == null) {
			return null;
		} else {
			return Long.toString(getterReturnResult.toEpochMilli());
		}
	}

	@Override
	public Instant unconvert(String object) {
		return unmarshall(Instant.class, object);
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
