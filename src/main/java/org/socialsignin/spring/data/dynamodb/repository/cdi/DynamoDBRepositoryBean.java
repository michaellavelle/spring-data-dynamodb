/**
 * Copyright © 2013 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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
package org.socialsignin.spring.data.dynamodb.repository.cdi;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.util.VersionInfoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBTemplate;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBRepositoryFactory;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.util.Assert;
import org.springframework.data.util.Version;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A bean which represents a DynamoDB repository.
 * 
 * @author Michael Lavelle
 * @param <T>
 *            The type of the repository.
 */
class DynamoDBRepositoryBean<T> extends CdiRepositoryBean<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBRepositoryBean.class);

	static {
		String awsSdkVersion = VersionInfoUtils.getVersion();
		String springDataVersion = Version.class.getPackage().getImplementationVersion();

		String thisSpecVersion = DynamoDBRepositoryBean.class.getPackage().getSpecificationVersion();
		String thisImplVersion = DynamoDBRepositoryBean.class.getPackage().getImplementationVersion();

		LOGGER.info("Spring Data DynamoDB Version: {} ({})", thisImplVersion, thisSpecVersion);
		LOGGER.info("Spring Data Version:          {}", springDataVersion);
		LOGGER.info("AWS SDK Version:              {}", awsSdkVersion);
		LOGGER.info("Java Version:                 {} - {} {}", System.getProperty("java.version"),
				System.getProperty("java.vm.name"), System.getProperty("java.vm.version"));
		LOGGER.info("Platform Details:             {} {}", System.getProperty("os.name"),
				System.getProperty("os.version"));

		if (!isCompatible(springDataVersion, thisSpecVersion)) {
			LOGGER.warn("This Spring Data DynamoDB implementation might not be compatible with the available Spring Data classes on the classpath!"
				+ System.getProperty("line.separator") + "NoDefClassFoundExceptions or similar might occur!");
		}
	}

	protected static boolean isCompatible(String spec, String impl) {
		if (spec == null && impl == null) {
			return false;
		} else if (spec == null) {
			spec = "";
		} else if (impl == null) {
			impl = "";
		}
		StringTokenizer specTokenizer = new StringTokenizer(spec, ".");
		StringTokenizer implTokenizer = new StringTokenizer(impl, ".");

		String specMajor = specTokenizer.hasMoreTokens() ? specTokenizer.nextToken() : "0";
		String specMinor = specTokenizer.hasMoreTokens() ? specTokenizer.nextToken() : "0";

		String implMajor = implTokenizer.hasMoreTokens() ? implTokenizer.nextToken() : "0";
		String implMinor = implTokenizer.hasMoreTokens() ? implTokenizer.nextToken() : "0";

		return specMajor.equals(implMajor) && specMinor.equals(implMinor);
	}

	private final Bean<AmazonDynamoDB> amazonDynamoDBBean;

	private final Bean<DynamoDBMapperConfig> dynamoDBMapperConfigBean;
		
	private final Bean<DynamoDBOperations> dynamoDBOperationsBean;

	/**
	 * Constructs a {@link DynamoDBRepositoryBean}.
	 * 
	 * @param beanManager
	 *            must not be {@literal null}.
	 * @param amazonDynamoDBBean
	 *            must not be {@literal null}.
	 * @param dynamoDBOperationsBean
	 *            must not be {@literal null}.
	 * @param qualifiers
	 *            must not be {@literal null}.
	 * @param repositoryType
	 *            must not be {@literal null}.
	 */
	DynamoDBRepositoryBean(BeanManager beanManager, Bean<AmazonDynamoDB> amazonDynamoDBBean,
						   Bean<DynamoDBMapperConfig> dynamoDBMapperConfigBean, Bean<DynamoDBOperations> dynamoDBOperationsBean,
						   Set<Annotation> qualifiers, Class<T> repositoryType) {

		super(qualifiers, repositoryType, beanManager);
		if (dynamoDBOperationsBean == null)
		{
			Assert.notNull(amazonDynamoDBBean, "amazonDynamoDBBean must not be null!");
		}
		else
		{
			Assert.isNull(amazonDynamoDBBean,"Cannot specify both amazonDynamoDB bean and dynamoDBOperationsBean in repository configuration");
			Assert.isNull(dynamoDBMapperConfigBean,"Cannot specify both dynamoDBMapperConfigBean bean and dynamoDBOperationsBean in repository configuration");

		}
		this.amazonDynamoDBBean = amazonDynamoDBBean;
		this.dynamoDBMapperConfigBean = dynamoDBMapperConfigBean;
		this.dynamoDBOperationsBean = dynamoDBOperationsBean;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.enterprise.context.spi.Contextual#create(javax.enterprise
	 * .context.spi.CreationalContext)
	 */
	@Override
	public T create(CreationalContext<T> creationalContext, Class<T> repositoryType) {

		// Get an instance from the associated AmazonDynamoDB bean.
		AmazonDynamoDB amazonDynamoDB = getDependencyInstance(amazonDynamoDBBean, AmazonDynamoDB.class);

		// Get an instance from the associated optional AmazonDynamoDB bean.
		DynamoDBMapperConfig dynamoDBMapperConfig = dynamoDBMapperConfigBean == null ? null : getDependencyInstance(
				dynamoDBMapperConfigBean, DynamoDBMapperConfig.class);
		
		DynamoDBOperations dynamoDBOperations = dynamoDBOperationsBean == null ? null
				:  getDependencyInstance(
						dynamoDBOperationsBean, DynamoDBOperations.class);

		if (dynamoDBOperations == null)
		{
			dynamoDBOperations = new DynamoDBTemplate(amazonDynamoDB,dynamoDBMapperConfig);
		}
		
		DynamoDBRepositoryFactory factory = new DynamoDBRepositoryFactory(dynamoDBOperations);
		return factory.getRepository(repositoryType);
	}

}
