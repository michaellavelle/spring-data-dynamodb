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
package org.socialsignin.spring.data.dynamodb.marshaller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("deprecation")
public class Instant2IsoDynamoDBMarshaller
		implements
			DynamoDBTypeConverter<String, Instant>,
			DynamoDBMarshaller<Instant> {

	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private DateTimeFormatter getDateFormat() {
		return DateTimeFormatter.ofPattern(PATTERN).withZone(ZoneOffset.UTC);
	}

	@Override
	public String convert(Instant object) {
		return marshall(object);
	}

	@Override
	public String marshall(Instant getterReturnResult) {
		if (getterReturnResult == null) {
			return null;
		} else {
			return getDateFormat().format(getterReturnResult);
		}
	}

	@Override
	public Instant unconvert(String object) {
		return unmarshall(Instant.class, object);
	}

	@Override
	public Instant unmarshall(Class<Instant> clazz, String obj) {
		if (StringUtils.isEmpty(obj)) {
			return null;
		} else {
			return Instant.from(getDateFormat().parse(obj));
		}
	}

}
