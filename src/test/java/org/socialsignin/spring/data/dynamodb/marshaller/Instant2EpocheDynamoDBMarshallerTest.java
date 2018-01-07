package org.socialsignin.spring.data.dynamodb.marshaller;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Instant2EpocheDynamoDBMarshallerTest {

	private Instant2EpocheDynamoDBMarshaller underTest;

	@Before
	public void setUp() {
		underTest = new Instant2EpocheDynamoDBMarshaller();
	}

	@Test
	public void testNullMarshall() {
		String actual = underTest.marshall(null);

		assertNull(actual);
	}
	
	@Test
	public void testMarshall() {
		assertEquals("0", underTest.marshall(Instant.ofEpochMilli(0)));
		assertEquals("0", underTest.convert(Instant.ofEpochMilli(0)));
	}
	
	@Test
	public void testUnmarshallNull() {
		Instant actual = underTest.unmarshall(Instant.class, null);

		assertNull(actual);
	}
	
	@Test
	public void testUnmarshall() {
		assertEquals(Instant.ofEpochMilli(0), underTest.unmarshall(Instant.class, "0"));
		assertEquals(Instant.ofEpochMilli(0), underTest.unconvert("0"));
	}

	@Test(expected = NumberFormatException.class)
	public void testUnmarshallGarbage() {
		underTest.unmarshall(Instant.class, "something");
	}
}
