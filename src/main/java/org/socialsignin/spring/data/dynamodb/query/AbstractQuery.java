/*
 * Copyright 2013 the original author or authors.
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
package org.socialsignin.spring.data.dynamodb.query;

import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBPersistentProperty;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * {@link DynamoDBPersistentProperty} implementation
 * 
 * @author Michael Lavelle
 */
public abstract class AbstractQuery<T> implements Query<T> {

	protected DynamoDBMapper dynamoDBMapper;
	protected Class<T> clazz;
	protected boolean scanEnabled = false;

	public void setScanEnabled(boolean scanEnabled) {
		this.scanEnabled = scanEnabled;
	}

	public boolean isScanEnabled() {
		return scanEnabled;
	}

	public AbstractQuery(DynamoDBMapper dynamoDBMapper, Class<T> clazz) {
		this.dynamoDBMapper = dynamoDBMapper;
		this.clazz = clazz;
	}

}
