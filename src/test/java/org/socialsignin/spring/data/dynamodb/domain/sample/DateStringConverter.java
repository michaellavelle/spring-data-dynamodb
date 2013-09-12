package org.socialsignin.spring.data.dynamodb.domain.sample;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class DateStringConverter implements DynamoDBMarshaller<Date> {

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	public String marshall(Date getterReturnResult) {
		return dateFormat.format(getterReturnResult);
	}

	@Override
	public Date unmarshall(Class<Date> clazz, String obj) {
		try {
			return dateFormat.parse(obj);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
