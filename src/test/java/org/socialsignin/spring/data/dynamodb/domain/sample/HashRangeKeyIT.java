/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/spring-data-dynamodb/spring-data-dynamodb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Show the usage of Hash+Range key as also how to use
 * XML based configuration
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/context/HashRangeKeyIT-context.xml"})
public class HashRangeKeyIT {

	@Autowired
	PlaylistRepository playlistRepository;

	@Test
	public void runCrudOperations() {
		final String displayName = "displayName" + UUID.randomUUID().toString();
		final String userName = "userName-" + UUID.randomUUID().toString();
		final String playlistName = "playlistName-" + UUID.randomUUID().toString();
		PlaylistId id = new PlaylistId(userName, playlistName);

		Optional<Playlist> actual = playlistRepository.findById(id);
		assertFalse(actual.isPresent());

		Playlist playlist = new Playlist(id);
		playlist.setDisplayName(displayName);

		playlistRepository.save(playlist);

		actual = playlistRepository.findById(id);
		assertTrue(actual.isPresent());
		assertEquals(displayName, actual.get().getDisplayName());
		assertEquals(id.getPlaylistName(), actual.get().getPlaylistName());
		assertEquals(id.getUserName(), actual.get().getUserName());
	}
}
