/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.config;

import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBRepositoryFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Michael Lavelle
 */
public class DynamoDBRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

	private static final String DEFAULT_AMAZON_DYNAMO_DB_BEAN_NAME = "amazonDynamoDB";

	private static final String DYNAMO_DB_MAPPER_CONFIG_REF = "dynamodb-mapper-config-ref";
	
	private static final String DYNAMO_DB_OPERATIONS_REF = "dynamodb-operations-ref";


	private static final String AMAZON_DYNAMODB_REF = "amazon-dynamodb-ref";

	@Override
	public String getRepositoryFactoryClassName() {
		return DynamoDBRepositoryFactoryBean.class.getName();
	}

	@Override
	public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
		AnnotationAttributes attributes = config.getAttributes();

		postProcess(builder, attributes.getString("amazonDynamoDBRef"), attributes.getString("dynamoDBMapperConfigRef"),attributes.getString("dynamoDBOperationsRef"));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.config.
	 * RepositoryConfigurationExtensionSupport
	 * #postProcess(org.springframework.beans
	 * .factory.support.BeanDefinitionBuilder,
	 * org.springframework.data.repository
	 * .config.XmlRepositoryConfigurationSource)
	 */
	@Override
	public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {

		Element element = config.getElement();

		ParsingUtils.setPropertyReference(builder, element, AMAZON_DYNAMODB_REF, "amazonDynamoDB");
		ParsingUtils.setPropertyReference(builder, element, DYNAMO_DB_MAPPER_CONFIG_REF, "dynamoDBMapperConfig");
		ParsingUtils.setPropertyReference(builder, element, DYNAMO_DB_OPERATIONS_REF, "dynamoDBOperations");


	}

	private void postProcess(BeanDefinitionBuilder builder, String amazonDynamoDBRef, String dynamoDBMapperConfigRef,String dynamoDBOperationsRef) {

		if (StringUtils.hasText(dynamoDBOperationsRef))
		{
			builder.addPropertyReference("dynamoDBOperations", dynamoDBOperationsRef);
			Assert.isTrue(!StringUtils.hasText(amazonDynamoDBRef),"Cannot specify both amazonDynamoDB bean and dynamoDBOperationsBean in repository configuration");
			Assert.isTrue(!StringUtils.hasText(dynamoDBMapperConfigRef),"Cannot specify both dynamoDBMapperConfigBean bean and dynamoDBOperationsBean in repository configuration");
		}
		else
		{
			
			amazonDynamoDBRef = StringUtils.hasText(amazonDynamoDBRef) ? amazonDynamoDBRef : DEFAULT_AMAZON_DYNAMO_DB_BEAN_NAME;

			builder.addPropertyReference("amazonDynamoDB", amazonDynamoDBRef);

			if (StringUtils.hasText(dynamoDBMapperConfigRef)) {
				builder.addPropertyReference("dynamoDBMapperConfig", dynamoDBMapperConfigRef);
			}
		}
		
	}

	@Override
	protected String getModulePrefix() {
		return "dynamoDB";
	}

}
