package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface InstallationRepository extends CrudRepository<Installation, String> {

    public List<Installation> findBySystemIdOrderByUpdatedAtDesc(String systemId);

}

