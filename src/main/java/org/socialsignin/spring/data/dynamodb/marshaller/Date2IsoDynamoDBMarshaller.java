package org.socialsignin.spring.data.dynamodb.marshaller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Date2IsoDynamoDBMarshaller extends DateDynamoDBMarshaller {

	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Override
	public DateFormat getDateFormat() {
		SimpleDateFormat df = new SimpleDateFormat(PATTERN);
		df.setTimeZone(UTC);
		return df;
	}
}
