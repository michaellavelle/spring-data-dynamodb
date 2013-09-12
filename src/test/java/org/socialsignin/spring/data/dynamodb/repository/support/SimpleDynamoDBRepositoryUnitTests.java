/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.PlaylistId;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.dao.EmptyResultDataAccessException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * Unit tests for {@link DynamoDBSimpleIdRepository}.
 * 
 * @author Michael Lavelle
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleDynamoDBRepositoryUnitTests {

	SimpleDynamoDBCrudRepository<User, Long> repoForEntityWithSimpleId;

	SimpleDynamoDBCrudRepository<Playlist, PlaylistId> repoForEntityWithCompositeId;

	@Mock
	DynamoDBMapper dynamoDBMapper;

	private User testUser;

	private Playlist testPlaylist;

	private PlaylistId testPlaylistId;

	@Mock
	DynamoDBEntityInformation<User, Long> entityWithSimpleIdInformation;

	@Mock
	DynamoDBEntityInformation<Playlist, PlaylistId> entityWithCompositeIdInformation;

	@Before
	public void setUp() {

		testUser = new User();

		testPlaylistId = new PlaylistId();
		testPlaylistId.setUserName("michael");
		testPlaylistId.setPlaylistName("playlist1");

		testPlaylist = new Playlist(testPlaylistId);

		when(entityWithSimpleIdInformation.getJavaType()).thenReturn(User.class);
		when(entityWithSimpleIdInformation.getHashKey(1l)).thenReturn(1l);

		when(entityWithCompositeIdInformation.getJavaType()).thenReturn(Playlist.class);
		when(entityWithCompositeIdInformation.getHashKey(testPlaylistId)).thenReturn("michael");
		when(entityWithCompositeIdInformation.getRangeKey(testPlaylistId)).thenReturn("playlist1");
		when(entityWithCompositeIdInformation.hasCompositeId()).thenReturn(true);

		repoForEntityWithSimpleId = new SimpleDynamoDBCrudRepository<User, Long>(entityWithSimpleIdInformation,
				dynamoDBMapper);
		repoForEntityWithCompositeId = new SimpleDynamoDBCrudRepository<Playlist, PlaylistId>(
				entityWithCompositeIdInformation, dynamoDBMapper);

		when(dynamoDBMapper.load(User.class, 1l)).thenReturn(testUser);
		when(dynamoDBMapper.load(Playlist.class, "michael", "playlist1")).thenReturn(testPlaylist);

	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityWithSimpleIdToDeleteDoesNotExist() {

		repoForEntityWithSimpleId.delete(4711L);
	}

	@Test
	public void findOneEntityWithSimpleId() {
		User user = repoForEntityWithSimpleId.findOne(1l);
		Mockito.verify(dynamoDBMapper).load(User.class,1l);
		assertEquals(testUser, user);
	}
	

	@Test
	public void findOneEntityWithCompositeId() {
		Playlist playlist = repoForEntityWithCompositeId.findOne(testPlaylistId);
		assertEquals(testPlaylist, playlist);
	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityWithCompositeIdToDeleteDoesNotExist() {

		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUser");
		playlistId.setPlaylistName("somePlaylistName");

		repoForEntityWithCompositeId.delete(playlistId);
	}
}
