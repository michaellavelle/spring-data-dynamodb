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
package org.socialsignin.spring.data.dynamodb.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.domain.sample.AuditableUser;
import org.socialsignin.spring.data.dynamodb.domain.sample.AuditableUserRepository;
import org.socialsignin.spring.data.dynamodb.domain.sample.CRUDOperationsIT;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.socialsignin.spring.data.dynamodb.utils.TableCreationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

/**
 * Integration tests for auditing via Java config.
 * 
 * @author Vito Limandibhrata
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DynamoDBLocalResource.class, AuditingViaJavaConfigRepositoriesIT.TestAppConfig.class})
@TestExecutionListeners(listeners = TableCreationListener.class, mergeMode = MERGE_WITH_DEFAULTS)

public class AuditingViaJavaConfigRepositoriesIT {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditingViaJavaConfigRepositoriesIT.class);

	@Configuration
	@EnableDynamoDBAuditing(auditorAwareRef = "auditorProvider")
	@EnableDynamoDBRepositories(mappingContextRef = "dynamoDBMappingContext", basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
	public static class TestAppConfig {

		@Bean(name = "auditorProvider")
		public AuditorAware<AuditableUser> auditorProvider() {
			LOGGER.info("auditorProvider");
			return mock(AuditorAware.class);
		}

		@Bean
		public DynamoDBMappingContext dynamoDBMappingContext() {
			DynamoDBMappingContext mappingContext = new DynamoDBMappingContext();
			// Register entity
			// TODO but this shouldn't be nessassary?!
			mappingContext.getPersistentEntity(AuditableUser.class);
			return mappingContext;
		}
	}

	@Autowired
	AuditableUserRepository auditableUserRepository;

	@Autowired
	AuditorAware<AuditableUser> auditorAware;

	AuditableUser auditor;

	@Autowired
	private AmazonDynamoDB ddb;

	@Before
	public void setUp() throws InterruptedException {
		CreateTableRequest ctr = new DynamoDBMapper(ddb).generateCreateTableRequest(AuditableUser.class);
		ctr.withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
		ddb.createTable(ctr);
		// Thread.sleep(5 * 1000);

		this.auditor = auditableUserRepository.save(new AuditableUser("auditor"));
		assertThat(this.auditor, is(notNullValue()));

		Optional<AuditableUser> auditorUser = auditableUserRepository.findById(this.auditor.getId());
		assertTrue(auditorUser.isPresent());

	}

	@Test
	public void basicAuditing() {

		doReturn(Optional.of(this.auditor.getId())).when(this.auditorAware).getCurrentAuditor();

		AuditableUser savedUser = auditableUserRepository.save(new AuditableUser("user"));

		assertThat(savedUser.getCreatedAt(), is(notNullValue()));
		assertThat(savedUser.getCreatedBy(), is(this.auditor.getId()));

		assertThat(savedUser.getLastModifiedAt(), is(notNullValue()));
		assertThat(savedUser.getLastModifiedBy(), is(this.auditor.getId()));

	}

}
