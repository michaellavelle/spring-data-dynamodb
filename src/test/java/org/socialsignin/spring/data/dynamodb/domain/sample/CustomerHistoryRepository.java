package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.springframework.data.repository.CrudRepository;

public interface CustomerHistoryRepository extends CrudRepository<CustomerHistory, CustomerHistoryId> {

    CustomerHistory findByTag(String tag);

}
