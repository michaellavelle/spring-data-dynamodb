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

/*
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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.data.annotation.Id;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

/**
 * {@link EntityInformation} implementation that inspects getters for an
 * annotation and invokes this getter's value to retrieve the id.
 * 
 * @author Michael Lavelle
 */
public class GetterReflectionEntityInformation<T, ID extends Serializable> extends AbstractEntityInformation<T, ID> {

	private static final Class<Id> DEFAULT_ID_ANNOTATION = Id.class;

	protected Method method;

	/**
	 * Creates a new {@link ReflectionEntityInformation} inspecting the given
	 * domain class for a getter carrying the {@link Id} annotation.
	 * 
	 * @param domainClass
	 *            must not be {@literal null}.
	 */
	public GetterReflectionEntityInformation(Class<T> domainClass) {
		this(domainClass, DEFAULT_ID_ANNOTATION);
	}

	/**
	 * Creates a new {@link GetterReflectionEntityInformation} inspecting the
	 * given domain class for a getter carrying the given annotation.
	 * 
	 * @param domainClass
	 *            must not be {@literal null}.
	 * @param annotation
	 *            must not be {@literal null}.
	 */
	public GetterReflectionEntityInformation(Class<T> domainClass, final Class<? extends Annotation> annotation) {

		super(domainClass);
		Assert.notNull(annotation);

		ReflectionUtils.doWithMethods(domainClass, new MethodCallback() {
			public void doWith(Method method) {
				if (method.getAnnotation(annotation) != null) {
					GetterReflectionEntityInformation.this.method = method;
					return;
				}
			}
		});

		Assert.notNull(this.method, String.format("No method annotated with %s found!", annotation.toString()));
		ReflectionUtils.makeAccessible(method);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.EntityInformation#getId(java
	 * .lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public ID getId(T entity) {
		return entity == null ? null : (ID) ReflectionUtils.invokeMethod(method, entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.EntityInformation#getIdType()
	 */
	@SuppressWarnings("unchecked")
	public Class<ID> getIdType() {
		return (Class<ID>) method.getReturnType();
	}
}
