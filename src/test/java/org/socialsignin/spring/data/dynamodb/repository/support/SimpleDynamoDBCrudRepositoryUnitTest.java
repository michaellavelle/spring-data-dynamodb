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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.PlaylistId;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link DynamoDBSimpleIdRepository}.
 * 
 * @author Michael Lavelle
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleDynamoDBCrudRepositoryUnitTest {

	SimpleDynamoDBCrudRepository<User, Long> repoForEntityWithOnlyHashKey;

	SimpleDynamoDBCrudRepository<Playlist, PlaylistId> repoForEntityWithHashAndRangeKey;

	@Mock
	DynamoDBOperations dynamoDBOperations;

	private User testUser;

	private Playlist testPlaylist;

	private PlaylistId testPlaylistId;
	
	@Mock
	EnableScanPermissions mockEnableScanPermissions;

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
		
		when(mockEnableScanPermissions.isFindAllUnpaginatedScanEnabled()).thenReturn(true);
		when(mockEnableScanPermissions.isDeleteAllUnpaginatedScanEnabled()).thenReturn(true);
		when(mockEnableScanPermissions.isCountUnpaginatedScanEnabled()).thenReturn(true);

		
		when(entityWithCompositeIdInformation.getJavaType()).thenReturn(Playlist.class);
		when(entityWithCompositeIdInformation.getHashKey(testPlaylistId)).thenReturn("michael");
		when(entityWithCompositeIdInformation.getRangeKey(testPlaylistId)).thenReturn("playlist1");
		when(entityWithCompositeIdInformation.isRangeKeyAware()).thenReturn(true);

		repoForEntityWithOnlyHashKey = new SimpleDynamoDBCrudRepository<User, Long>(entityWithSimpleIdInformation,
				dynamoDBOperations,mockEnableScanPermissions);
		repoForEntityWithHashAndRangeKey = new SimpleDynamoDBCrudRepository<Playlist, PlaylistId>(
				entityWithCompositeIdInformation, dynamoDBOperations,mockEnableScanPermissions);

		when(dynamoDBOperations.load(User.class, 1l)).thenReturn(testUser);
		when(dynamoDBOperations.load(Playlist.class, "michael", "playlist1")).thenReturn(testPlaylist);

	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityOnlyHashKeyToDeleteDoesNotExist() {

		repoForEntityWithOnlyHashKey.deleteById(4711L);
	}

	@Test
	public void findOneEntityWithOnlyHashKey() {
		Optional<User> user = repoForEntityWithOnlyHashKey.findById(1l);
		Mockito.verify(dynamoDBOperations).load(User.class,1l);
		assertEquals(testUser, user.get());
	}
	

	@Test
	public void findOneEntityWithHashAndRangeKey() {
		Optional<Playlist> playlist = repoForEntityWithHashAndRangeKey.findById(testPlaylistId);
		assertEquals(testPlaylist, playlist.get());
	}

	/**
	 * @see DATAJPA-177
	 */
	@Test(expected = EmptyResultDataAccessException.class)
	public void throwsExceptionIfEntityWithHashAndRangeKeyToDeleteDoesNotExist() {

		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUser");
		playlistId.setPlaylistName("somePlaylistName");

		repoForEntityWithHashAndRangeKey.deleteById(playlistId);
	}
}
