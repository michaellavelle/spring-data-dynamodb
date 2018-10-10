/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
