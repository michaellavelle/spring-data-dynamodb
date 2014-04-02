package org.socialsignin.spring.data.dynamodb.mapping.event;
/*
 * Copyright 2012 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;


/**
 * javax.validation dependant entities validator. When it is registered as Spring component its automatically invoked
 * before entities are saved in database.
 * 
 * @author Michael Lavelle
 */
public class ValidatingDynamoDBEventListener extends AbstractDynamoDBEventListener<Object> {

	private static final Logger LOG = LoggerFactory.getLogger(ValidatingDynamoDBEventListener.class);

	private final Validator validator;

	/**
	 * Creates a new {@link ValidatingDynamoDBEventListener} using the given {@link Validator}.
	 * 
	 * @param validator must not be {@literal null}.
	 */
	public ValidatingDynamoDBEventListener(Validator validator) {
		Assert.notNull(validator);
		this.validator = validator;
	}

	/*
	 * (non-Javadoc)
	 * @see org.socialsignin.spring.data.dynamodb.mapping.event.AbstractDynamoDBEventListener#onBeforeSave(java.lang.Object)
	 */
	@Override
	public void onBeforeSave(Object source) {

		LOG.debug("Validating object: {}", source);
		
		List<String> messages = new ArrayList<String>();
		Set<ConstraintViolation<Object>> violations = validator.validate(source);
		Set<ConstraintViolation<?>> genericViolationSet = new HashSet<ConstraintViolation<?>>();
		if (!violations.isEmpty()) {
			for (ConstraintViolation<?> v : violations) {
				genericViolationSet.add(v);
				messages.add(v.toString());
			}
			LOG.info("During object: {} validation violations found: {}", source, violations);
			throw new ConstraintViolationException(messages.toString(),genericViolationSet);
		}
	}
}
