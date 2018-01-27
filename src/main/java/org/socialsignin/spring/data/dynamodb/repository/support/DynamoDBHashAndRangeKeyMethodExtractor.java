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
package org.socialsignin.spring.data.dynamodb.repository.support;

import org.springframework.data.repository.core.EntityMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Michael Lavelle
 */
public interface DynamoDBHashAndRangeKeyMethodExtractor<ID> extends EntityMetadata<ID> {

	Method getHashKeyMethod();

	Method getRangeKeyMethod();
	
	Field getHashKeyField();

	Field getRangeKeyField();

}
