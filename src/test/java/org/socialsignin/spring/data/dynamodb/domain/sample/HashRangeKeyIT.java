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
