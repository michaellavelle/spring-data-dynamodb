package org.socialsignin.spring.data.dynamodb.marshaller;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.util.StringUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class Instant2IsoDynamoDBMarshaller implements DynamoDBMarshaller<Instant> {

	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private DateTimeFormatter getDateFormat() {
		return DateTimeFormatter.ofPattern(PATTERN)
			.withZone(ZoneOffset.UTC);
	}

	@Override
	public String marshall(Instant getterReturnResult) {
		if (getterReturnResult == null) {
			return null;
		} else {
			return getDateFormat().format(getterReturnResult);
		}
	}

	@Override
	public Instant unmarshall(Class<Instant> clazz, String obj) {
		if (StringUtils.isEmpty(obj)) {
			return null;
		} else {
			return Instant.from(getDateFormat().parse(obj));
		}
	}

}
