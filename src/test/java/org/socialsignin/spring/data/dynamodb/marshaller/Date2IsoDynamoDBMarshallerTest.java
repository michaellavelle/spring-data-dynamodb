package org.socialsignin.spring.data.dynamodb.marshaller;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Date2IsoDynamoDBMarshallerTest {

	private Date2IsoDynamoDBMarshaller underTest;

	@Before
	public void setUp() {
		underTest = new Date2IsoDynamoDBMarshaller();
	}

	@Test
	public void testNullMarshall() {
		String actual = underTest.marshall(null);

		assertNull(actual);
	}
	
	@Test
	public void testMarshall() {
		String actual = underTest.marshall(new Date(0));

		assertEquals("1970-01-01T00:00:00.000Z", actual);
	}
	
	@Test
	public void testUnmarshallNull() {
		Date actual = underTest.unmarshall(Date.class, null);

		assertNull(actual);
	}
	
	@Test
	public void testUnmarshall() {
		Date actual = underTest.unmarshall(Date.class, "1970-01-01T00:00:00.000Z");

		assertEquals(new Date(0), actual);
	}

	@Test(expected = RuntimeException.class)
	public void testUnmarshallGarbage() {
		underTest.unmarshall(Date.class, "something");
	}
}
