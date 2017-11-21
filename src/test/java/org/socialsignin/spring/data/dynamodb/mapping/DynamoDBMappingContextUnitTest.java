/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.mapping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link DynamoDBMappingContext}.
 * 
 * @author Michael Lavelle
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamoDBMappingContextUnitTest {

	DynamoDBMappingContext context;

	@Before
	public void setUp() {
		context = new DynamoDBMappingContext();
	}

	@Test
	public void setsUpMappingContextCorrectly() {

		DynamoDBPersistentEntityImpl<?> entity = context.getPersistentEntity(User.class);
		assertThat(entity, is(notNullValue()));
	}

	// @Test
	public void detectsIdProperty() {

		DynamoDBPersistentEntityImpl<?> entity = context.getPersistentEntity(User.class);
		assertThat(entity.getIdProperty(), is(notNullValue()));
	}

}
