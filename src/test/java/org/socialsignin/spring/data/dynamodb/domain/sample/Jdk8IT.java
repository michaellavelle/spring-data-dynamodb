/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/spring-data-dynamodb/spring-data-dynamodb)
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
package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests JDK8 features of spring-data
 * @see <a href="https://github.com/spring-projects/spring-data-examples/tree/master/jpa/java8">
 * github.com/spring-projects/spring-data-examples/master/jpa/java8</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DynamoDBResource.class, Jdk8IT.TestAppConfig.class})
public class Jdk8IT {

	@Configuration
	@EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
	public static class TestAppConfig {
	}

	@Autowired
	UserRepository userRepository;

	@Test
	public void testOptionalKey() {
		final Date joinDate = new Date(1000);
		final String id = "testOptionalKey";
		Optional<User> result = userRepository.findById(id);

		assertNotNull(result);
		assertEquals(result, Optional.empty());

		User newUser = new User();
		newUser.setId(id);
		newUser.setName(UUID.randomUUID().toString());
		newUser.setJoinDate(joinDate);

		User savedEntity = userRepository.save(newUser);

		result = userRepository.findById(id);
		assertNotNull(result);
		assertEquals(savedEntity, result.get());
		assertEquals(joinDate, result.get().getJoinDate());
	}
	
	@Test
	public void testOptionalFilter() {
        final Date joinDate = new Date(2000);
        final String id = "testOptionalFilter";
        final String name = UUID.randomUUID().toString();
        Optional<User> result = userRepository.findByName(name);

        assertNotNull(result);
        assertEquals(result, Optional.empty());

        User newUser = new User();
        newUser.setId(id);
        newUser.setName(name);
        newUser.setJoinDate(joinDate);

        User savedEntity = userRepository.save(newUser);

        result = userRepository.findByName(name);
        assertNotNull(result);
        assertEquals(savedEntity, result.get());
        assertEquals(joinDate, result.get().getJoinDate());
	}

	@Test
	public void testInstantQuery() {
		final Instant leaveDate = Instant.ofEpochMilli(2000);

		User newUser = new User();
		newUser.setId(UUID.randomUUID().toString());
		newUser.setLeaveDate(leaveDate);
		userRepository.save(newUser);
		
		List<User> results = userRepository.findByLeaveDate(leaveDate);
		assertEquals(1, results.size());
		
		User result = results.get(0);
		assertNotNull(result.getId());
		assertEquals(leaveDate, result.getLeaveDate());
	}
}
