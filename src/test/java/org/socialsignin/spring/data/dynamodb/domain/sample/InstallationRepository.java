package org.socialsignin.spring.data.dynamodb.domain.sample;

import java.util.List;
import org.springframework.data.repository.CrudRepository;


public interface InstallationRepository extends CrudRepository<Installation, String> {

    public List<Installation> findBySystemIdOrderByUpdatedAtDesc(String systemId);

}

