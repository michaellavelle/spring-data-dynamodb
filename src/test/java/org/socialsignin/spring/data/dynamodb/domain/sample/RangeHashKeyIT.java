package org.socialsignin.spring.data.dynamodb.domain.sample;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.core.ConfigurationTI;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConfigurationTI.class, RangeHashKeyIT.TestAppConfig.class})
public class RangeHashKeyIT {

	@Configuration
	@EnableDynamoDBRepositories(
	basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
	public static class TestAppConfig {
	}

	@Autowired
	PlaylistRepository playlistRepository;

	@Test
	public void runCrudOperations() {
		final String displayName = "displayName" + UUID.randomUUID().toString();
		final String userName = "userName-" + UUID.randomUUID().toString();
		final String playlistName = "playlistName-" + UUID.randomUUID().toString();
		PlaylistId id = new PlaylistId(userName, playlistName);

		Playlist actual = playlistRepository.findOne(id);
		assertNull(actual);

		Playlist playlist = new Playlist(id);
		playlist.setDisplayName(displayName);

		playlistRepository.save(playlist);

		actual = playlistRepository.findOne(id);
		assertNotNull(actual);
		assertEquals(displayName, actual.getDisplayName());
		assertEquals(id.getPlaylistName(), actual.getPlaylistName());
		assertEquals(id.getUserName(), actual.getUserName());
	}
}
