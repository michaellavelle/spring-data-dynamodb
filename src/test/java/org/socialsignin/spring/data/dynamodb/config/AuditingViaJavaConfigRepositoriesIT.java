/*
 * Copyright 2013-2014 the original author or authors.
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
package org.socialsignin.spring.data.dynamodb.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.domain.sample.AuditableUser;
import org.socialsignin.spring.data.dynamodb.domain.sample.AuditableUserRepository;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for auditing via Java config.
 * 
 * @author Vito Limandibhrata
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AuditingViaJavaConfigRepositoriesIT.class)
@Configuration
@EnableDynamoDBAuditing(auditorAwareRef = "auditorProvider")
@EnableDynamoDBRepositories(basePackageClasses = AuditableUserRepository.class)
public class AuditingViaJavaConfigRepositoriesIT extends AbstractDynamoDBConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditingViaJavaConfigRepositoriesIT.class);

	private static final String DYNAMODB_PORT_PROPERTY = "dynamodb.port";
	private static final String PORT = System.getProperty(DYNAMODB_PORT_PROPERTY);

	@Autowired
	AuditableUserRepository auditableUserRepository;

	@Autowired
	AuditorAware<AuditableUser> auditorAware;

	AuditableUser auditor;

	@Override
	protected String[] getMappingBasePackages() {
		return new String[]{"org.socialsignin.spring.data.dynamodb.domain.sample"};
	}

	@Bean(name="auditorProvider")
	@SuppressWarnings("unchecked")
	public AuditorAware<AuditableUser> auditorProvider() {
		LOGGER.info("auditorProvider");
		return mock(AuditorAware.class);
	}

	@Bean
	@Override
	public AmazonDynamoDB amazonDynamoDB() {
		Assert.notNull(PORT, "System property '" + DYNAMODB_PORT_PROPERTY + " not set!");

		AmazonDynamoDB amazonDynamoDB = new AmazonDynamoDBClient(amazonAWSCredentials());
		amazonDynamoDB.setEndpoint(String.format("http://localhost:%s", PORT));
		return amazonDynamoDB;
	}

	/**
	 * Must return the same credential as {@link org.socialsignin.spring.data.dynamodb.core.ConfigurationTI}
	 * otherwise the repository will connect to different local DynamoDB instance
	 * hence it will return no table found
	 *
	 * @return
	 */
	@Bean
	@Override
	public AWSCredentials amazonAWSCredentials() {
		return new BasicAWSCredentials("AWS-Key", "");
	}

	@Before
	public void setup() {
		auditableUserRepository.deleteAll();
		this.auditor = auditableUserRepository.save(new AuditableUser("auditor"));
		assertThat(this.auditor, is(notNullValue()));

        Optional<AuditableUser> auditorUser = auditableUserRepository.findById(this.auditor.getId());
        assertTrue(auditorUser.isPresent());

	}

	@Test
	public void basicAuditing() {

		doReturn(this.auditor.getId()).when(this.auditorAware).getCurrentAuditor();

		AuditableUser savedUser = auditableUserRepository.save(new AuditableUser("user"));

		assertThat(savedUser.getCreatedAt(), is(notNullValue()));
		assertThat(savedUser.getCreatedBy(), is(this.auditor.getId()));

        assertThat(savedUser.getLastModifiedAt(), is(notNullValue()));
        assertThat(savedUser.getLastModifiedBy(), is(this.auditor.getId()));

	}


}
