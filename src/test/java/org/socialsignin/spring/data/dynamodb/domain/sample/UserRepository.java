package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, String> {

	// CRUD method using Optional
	Optional<User> findOne(String id);

	@EnableScan
	List<User> findByLeaveDate(Instant leaveDate);

	@EnableScan
	Optional<User> findByName(String name);

	<T extends User> User save(T entity);
}
