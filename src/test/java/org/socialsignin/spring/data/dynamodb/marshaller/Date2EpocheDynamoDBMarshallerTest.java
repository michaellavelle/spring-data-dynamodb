package org.socialsignin.spring.data.dynamodb.marshaller;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Date2EpocheDynamoDBMarshallerTest {

	private Date2EpocheDynamoDBMarshaller underTest;

	@Before
	public void setUp() {
		underTest = new Date2EpocheDynamoDBMarshaller();
	}

	@Test
	public void testNullMarshall() {
		String actual = underTest.marshall(null);

		assertNull(actual);
	}
	
	@Test
	public void testMarshall() {
		assertEquals("0", underTest.marshall(new Date(0)));
		assertEquals("0", underTest.convert(new Date(0)));
	}
	
	@Test
	public void testUnmarshallNull() {
		Date actual = underTest.unmarshall(Date.class, null);

		assertNull(actual);
	}
	
	@Test
	public void testUnmarshall() {
		assertEquals(new Date(0), underTest.unmarshall(Date.class, "0"));
		assertEquals(new Date(0), underTest.unconvert("0"));;

	}

	@Test(expected = NumberFormatException.class)
	public void testUnmarshallGarbage() {
		underTest.unmarshall(Date.class, "something");
	}
}
