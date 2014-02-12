package org.socialsignin.spring.data.dynamodb.repository.query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;


public abstract class AbstractDynamoDBQueryCriteriaUnitTests<C extends AbstractDynamoDBQueryCriteria<?,?>> {

	protected C criteria;

	@Test
	public void addAttributeValueTest_WhenValueIsSingleDate() throws ParseException
	{
		// Setup date formats for EST and UCT
		DateFormat estDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		estDateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
		DateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UCT"));
		
		// Generate a Date object using a fixed EST date string
		String dateInESTString = "2014-02-12T02:34:00.000Z";
		Date now = estDateFormat.parse(dateInESTString);

		// Generate a date string for this Date in UTC
		String dateInUCTString = utcDateFormat.format(now);

		// Sanity check - confirm that the EST and UTC strings aren't equal
		Assert.assertNotEquals(estDateFormat, dateInUCTString);
	
		List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>();

		// Add "now" as an attribute value - a Date object originally generated from EST string
		criteria.addAttributeValue(attributeValueList , now, "someDateProperty", Date.class, false);
		
		AttributeValue resultingValue = attributeValueList.get(0);
		
		// Ensuring that the resulting AttributeValue is encoded as a UTC string
		Assert.assertEquals(dateInUCTString,resultingValue.getS());
	}
	
	@Test
	public void addAttributeValueTest_WhenValueIsDateCollection() throws ParseException
	{
		// Setup date formats for EST and UCT
		DateFormat estDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		estDateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
		DateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UCT"));
		
		// Generate a Date object using a fixed EST date string
		String dateInESTString = "2014-02-12T02:34:00.000Z";
		Date now = estDateFormat.parse(dateInESTString);

		// Generate a date string for this Date in UTC
		String dateInUCTString = utcDateFormat.format(now);

		// Sanity check - confirm that the EST and UTC strings aren't equal
		Assert.assertNotEquals(estDateFormat, dateInUCTString);
	
		List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>();

		List<Date> dateList = new ArrayList<Date>();
		dateList.add(now);
		
		List<String> dateStringList = new ArrayList<String>();
		dateStringList.add(dateInUCTString);
		
		// Add "now" as an attribute value - a Date object originally generated from EST string
		criteria.addAttributeValue(attributeValueList , dateList, "someDateProperty", Date.class, true);
		
		AttributeValue resultingValue = attributeValueList.get(0);
		
		// Ensuring that the resulting AttributeValue is encoded as a UTC string
		Assert.assertEquals(dateStringList,resultingValue.getSS());
	}
	
}
