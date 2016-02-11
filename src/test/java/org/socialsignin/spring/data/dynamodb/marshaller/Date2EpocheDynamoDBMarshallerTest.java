package org.socialsignin.spring.data.dynamodb.marshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

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
		String actual = underTest.marshall(new Date(0));

		assertEquals("0", actual);
	}
	
	@Test
	public void testUnmarshallNull() {
		Date actual = underTest.unmarshall(Date.class, null);

		assertNull(actual);
	}
	
	@Test
	public void testUnmarshall() {
		Date actual = underTest.unmarshall(Date.class, "0");

		assertEquals(new Date(0), actual);
	}

	@Test(expected = NumberFormatException.class)
	public void testUnmarshallGarbage() {
		underTest.unmarshall(Date.class, "something");
	}
}
