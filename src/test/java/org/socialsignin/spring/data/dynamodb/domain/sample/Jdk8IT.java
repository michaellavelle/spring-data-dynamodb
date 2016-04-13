package org.socialsignin.spring.data.dynamodb.domain.sample;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.core.ConfigurationTI;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests JDK8 features of spring-data
 * @see <a href="https://github.com/spring-projects/spring-data-examples/tree/master/jpa/java8">
 * github.com/spring-projects/spring-data-examples/master/jpa/java8</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConfigurationTI.class, Jdk8IT.TestAppConfig.class})
public class Jdk8IT {

	@Configuration
	@EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
	public static class TestAppConfig {
	}

	@Autowired
	UserRepository userRepository;

	@Test
	public void testOptional() {
		final Date joinDate = new Date(1000);
		final String id = "testOptional";
		Optional<User> result = userRepository.findOne(id);

		assertNotNull(result);
		assertEquals(result, Optional.empty());

		User newUser = new User();
		newUser.setId(id);
		newUser.setName(UUID.randomUUID().toString());
		newUser.setJoinDate(joinDate);

		User savedEntity = userRepository.save(newUser);

		result = userRepository.findOne(id);
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
