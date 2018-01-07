package org.socialsignin.spring.data.dynamodb.marshaller;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Instant2IsoDynamoDBMarshallerTest {

	private Instant2IsoDynamoDBMarshaller underTest;

	@Before
	public void setUp() {
		underTest = new Instant2IsoDynamoDBMarshaller();
	}

	@Test
	public void testNullMarshall() {
		String actual = underTest.marshall(null);

		assertNull(actual);
	}
	
	@Test
	public void testMarshall() {
		assertEquals("1970-01-01T00:00:00.000Z", underTest.marshall(Instant.ofEpochMilli(0)));
		assertEquals("1970-01-01T00:00:00.000Z", underTest.convert(Instant.ofEpochMilli(0)));
	}
	
	@Test
	public void testUnmarshallNull() {
		Instant actual = underTest.unmarshall(Instant.class, null);

		assertNull(actual);
	}
	
	@Test
	public void testUnmarshall() {
		assertEquals(Instant.ofEpochMilli(0), underTest.unmarshall(Instant.class, "1970-01-01T00:00:00.000Z"));
		assertEquals(Instant.ofEpochMilli(0), underTest.unconvert("1970-01-01T00:00:00.000Z"));
	}

	@Test(expected = RuntimeException.class)
	public void testUnmarshallGarbage() {
		underTest.unmarshall(Instant.class, "something");
	}
}
