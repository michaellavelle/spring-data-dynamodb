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

import java.util.List;



public interface Query<T> {

	/**
     * Execute a SELECT query and return the query results
     * as a List.
     * @return a list of the results
     * @throws IllegalStateException if called for a Java
     *    Persistence query language UPDATE or DELETE statement
     */
    public List<T> getResultList();

    /**
     * Execute a SELECT query that returns a single result.
     * @return the result
     */
    public T getSingleResult();


    public void setScanEnabled(boolean scanEnabled);
    public void setScanCountEnabled(boolean scanCountEnabled);

}
