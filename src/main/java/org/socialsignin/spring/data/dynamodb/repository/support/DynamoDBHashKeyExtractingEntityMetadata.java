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
package org.socialsignin.spring.data.dynamodb.repository.support;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import org.springframework.data.repository.core.EntityMetadata;

import java.util.Map;

/**
 * Obtains basic hash key-related metadata about a DynamoDBEntity, such as
 * whether properties have overridden attribute names or have custom marshallers
 * assigned, whether a property is a hash key property or a composite id
 * property, and generates a hash key prototype entity given a hash key.
 * 
 * @author Michael Lavelle
 */
public interface DynamoDBHashKeyExtractingEntityMetadata<T> extends EntityMetadata<T> {

	public String getOverriddenAttributeName(String propertyName);

	public DynamoDBMarshaller<?> getMarshallerForProperty(String propertyName);

	public boolean isHashKeyProperty(String propertyName);

	public String getHashKeyPropertyName();

	public String getDynamoDBTableName();

	Map<String, String[]> getGlobalSecondaryIndexNamesByPropertyName();
	
	boolean isGlobalIndexHashKeyProperty(String propertyName);

	boolean isGlobalIndexRangeKeyProperty(String propertyName);

}
