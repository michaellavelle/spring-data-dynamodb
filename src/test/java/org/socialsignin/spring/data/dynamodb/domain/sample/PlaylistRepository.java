package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.springframework.data.repository.CrudRepository;

public interface PlaylistRepository extends CrudRepository<Playlist, PlaylistId> {

}
