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
public class SimpleDynamoDBPagingAndSortingRepositoryUnitTests {

	SimpleDynamoDBPagingAndSortingRepository<User, Long> repoForEntityWithOnlyHashKey;

	SimpleDynamoDBPagingAndSortingRepository<Playlist, PlaylistId> repoForEntityWithHashAndRangeKey;

	@Mock
	DynamoDBMapper dynamoDBMapper;

	private User testUser;

	private Playlist testPlaylist;

	private PlaylistId testPlaylistId;

	@Mock
	DynamoDBEntityInformation<User, Long> entityWithOnlyHashKeyInformation;

	@Mock
	DynamoDBEntityInformation<Playlist, PlaylistId> entityWithHashAndRangeKeyInformation;

	@Before
	public void setUp() {

		testUser = new User();

		testPlaylistId = new PlaylistId();
		testPlaylistId.setUserName("michael");
		testPlaylistId.setPlaylistName("playlist1");

		testPlaylist = new Playlist(testPlaylistId);

		when(entityWithOnlyHashKeyInformation.getJavaType()).thenReturn(User.class);
		when(entityWithOnlyHashKeyInformation.getHashKey(1l)).thenReturn(1l);

		when(entityWithHashAndRangeKeyInformation.getJavaType()).thenReturn(Playlist.class);
		when(entityWithHashAndRangeKeyInformation.getHashKey(testPlaylistId)).thenReturn("michael");
		when(entityWithHashAndRangeKeyInformation.getRangeKey(testPlaylistId)).thenReturn("playlist1");
		when(entityWithHashAndRangeKeyInformation.isRangeKeyAware()).thenReturn(true);

		repoForEntityWithOnlyHashKey = new SimpleDynamoDBPagingAndSortingRepository<User, Long>(entityWithOnlyHashKeyInformation,
				dynamoDBMapper);
		repoForEntityWithHashAndRangeKey = new SimpleDynamoDBPagingAndSortingRepository<Playlist, PlaylistId>(
				entityWithHashAndRangeKeyInformation, dynamoDBMapper);

		when(dynamoDBMapper.load(User.class, 1l)).thenReturn(testUser);
		when(dynamoDBMapper.load(Playlist.class, "michael", "playlist1")).thenReturn(testPlaylist);

	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityWithOnlyHashKeyToDeleteDoesNotExist() {

		repoForEntityWithOnlyHashKey.delete(4711L);
	}

	@Test
	public void findOneEntityWithOnlyHashKey() {
		User user = repoForEntityWithOnlyHashKey.findOne(1l);
		Mockito.verify(dynamoDBMapper).load(User.class,1l);
		assertEquals(testUser, user);
	}
	

	@Test
	public void findOneEntityWithHashAndRangeKey() {
		Playlist playlist = repoForEntityWithHashAndRangeKey.findOne(testPlaylistId);
		assertEquals(testPlaylist, playlist);
	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityWithHashAndRangeKeyToDeleteDoesNotExist() {

		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUser");
		playlistId.setPlaylistName("somePlaylistName");

		repoForEntityWithHashAndRangeKey.delete(playlistId);
	}
}
