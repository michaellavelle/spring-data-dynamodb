package org.socialsignin.spring.data.dynamodb.marshaller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public abstract class DateDynamoDBMarshaller implements DynamoDBTypeConverter<String, Date>, DynamoDBMarshaller<Date> {

	public abstract DateFormat getDateFormat();

	@Override
	public String convert(Date object) {
		return marshall(object);
	}

	@Override
	public String marshall(Date getterReturnResult) {
		if (getterReturnResult == null) {
			return null;
		} else {
			return getDateFormat().format(getterReturnResult);
		}
	}

	@Override
	public Date unconvert(String object) {
		return unmarshall(Date.class, object);
	}

	@Override
	public Date unmarshall(Class<Date> clazz, String obj) {
		if (StringUtils.isEmpty(obj)) {
			return null;
		} else {
			try {
				return getDateFormat().parse(obj);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
