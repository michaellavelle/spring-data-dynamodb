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
     * @throws EntityNotFoundException if there is no result
     * @throws NonUniqueResultException if more than one result
     * @throws IllegalStateException if called for a Java 
     *    Persistence query language UPDATE or DELETE statement
     */
    public T getSingleResult();
}
