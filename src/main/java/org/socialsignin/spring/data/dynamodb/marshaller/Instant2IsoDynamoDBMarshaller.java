package org.socialsignin.spring.data.dynamodb.marshaller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Instant2IsoDynamoDBMarshaller implements DynamoDBTypeConverter<String, Instant>, DynamoDBMarshaller<Instant> {

	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private DateTimeFormatter getDateFormat() {
		return DateTimeFormatter.ofPattern(PATTERN)
			.withZone(ZoneOffset.UTC);
	}

	@Override
	public String convert(Instant object) {
		return marshall(object);
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
	public Instant unconvert(String object) {
		return unmarshall(Instant.class, object);
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
