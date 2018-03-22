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
package org.socialsignin.spring.data.dynamodb.mapping.event;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;


/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class AfterScanEvent<T> extends DynamoDBMappingEvent<PaginatedScanList<T>> {

	private static final long serialVersionUID = 1L;

	public AfterScanEvent(PaginatedScanList<T> source) {
		super(source);
	}

}