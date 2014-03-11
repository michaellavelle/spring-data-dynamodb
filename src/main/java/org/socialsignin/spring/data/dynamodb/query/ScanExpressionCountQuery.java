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

import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

public class ScanExpressionCountQuery<T> extends AbstractSingleEntityQuery<Long> implements Query<Long>{

	private DynamoDBScanExpression scanExpression;
	
	private Class<T> domainClass;
	
	public ScanExpressionCountQuery(DynamoDBMapper dynamoDBMapper, Class<T> clazz,DynamoDBScanExpression scanExpression) {
		super(dynamoDBMapper, Long.class);
		this.scanExpression = scanExpression;
		this.domainClass = clazz;
	}

	@Override
	public Long getSingleResult() {
		assertScanCountEnabled(isScanCountEnabled());
		return new Long(dynamoDBMapper.count(domainClass,scanExpression));
	}
	
	public void assertScanCountEnabled(boolean scanCountEnabled)
	{
		Assert.isTrue(scanCountEnabled,"Scanning for the total counts for this query is not enabled.  " +
				"To enable annotate your repository method with @EnableScanCount, or " +
				"enable scanning for all repository methods by annotating your repository interface with @EnableScanCount");
	}

}
