package org.socialsignin.spring.data.dynamodb.domain.sample;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, String> {

    // CRUD method using Optional
    Optional<User> findOne(String id);

    <T extends User> User save(T entity);
}
