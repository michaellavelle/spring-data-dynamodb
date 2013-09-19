package org.socialsignin.spring.data.dynamodb.repository.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.DynamoDBYearMarshaller;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.PlaylistId;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBHashAndRangeKeyExtractingEntityInformation;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.RepositoryQuery;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

@RunWith(MockitoJUnitRunner.class)
public class PartTreeDynamoDBQueryUnitTests {

	private RepositoryQuery partTreeDynamoDBQuery;

	@Mock
	private DynamoDBMapper mockDynamoDBMapper;

	@Mock
	private DynamoDBQueryMethod<User, String> mockDynamoDBUserQueryMethod;

	@Mock
	private DynamoDBEntityInformation<User,String> mockUserEntityMetadata;

	@Mock
	private DynamoDBQueryMethod<Playlist, PlaylistId> mockDynamoDBPlaylistQueryMethod;

	@Mock
	private DynamoDBHashAndRangeKeyExtractingEntityInformation<Playlist,PlaylistId> mockPlaylistEntityMetadata;

	@Mock
	@SuppressWarnings("rawtypes")
	private Parameters mockParameters;

	@Mock
	private User mockUser;

	@Mock
	private Playlist mockPlaylist;

	@SuppressWarnings("rawtypes")
	@Mock
	private PaginatedScanList mockUserScanResults;

	@SuppressWarnings("rawtypes")
	@Mock
	private PaginatedScanList mockPlaylistScanResults;

	@SuppressWarnings("rawtypes")
	@Mock
	private PaginatedQueryList mockPlaylistQueryResults;
	
	@SuppressWarnings("rawtypes")
	@Mock
	private DynamoDBCriteria mockCriteria;
	

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {

		Mockito.when(mockPlaylistEntityMetadata.isCompositeHashAndRangeKeyProperty("playlistId")).thenReturn(true);
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropertyName()).thenReturn("userName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKeyPropertyName()).thenReturn("playlistName");
		Mockito.when(mockPlaylistEntityMetadata.getIndexRangeKeyPropertyNames()).thenReturn(new HashSet<String>());
		Mockito.when(mockDynamoDBUserQueryMethod.getEntityInformation()).thenReturn(mockUserEntityMetadata);
		Mockito.when(mockDynamoDBUserQueryMethod.getParameters()).thenReturn(mockParameters);
		Mockito.when(mockDynamoDBPlaylistQueryMethod.getEntityInformation()).thenReturn(mockPlaylistEntityMetadata);
		Mockito.when(mockDynamoDBPlaylistQueryMethod.getParameters()).thenReturn(mockParameters);
		Mockito.when(mockUserEntityMetadata.getHashKeyPropertyName()).thenReturn("id");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T, ID extends Serializable> void setupCommonMocksForThisRepositoryMethod(
			DynamoDBEntityInformation<T, ID> mockEntityMetadata, DynamoDBQueryMethod<T, ID> mockDynamoDBQueryMethod,
			Class<T> clazz, String repositoryMethodName, int numberOfParameters, String hashKeyProperty,
			String rangeKeyProperty) {

		if (rangeKeyProperty != null) {
			Mockito.when(mockEntityMetadata.isRangeKeyAware()).thenReturn(true);
		}

		Class returnedObjectClass = clazz;
		Mockito.when(mockDynamoDBQueryMethod.getEntityType()).thenReturn(clazz);
		Mockito.when(mockDynamoDBQueryMethod.getName()).thenReturn(repositoryMethodName);
		Mockito.when(mockParameters.getNumberOfParameters()).thenReturn(numberOfParameters);
		Mockito.when(mockDynamoDBQueryMethod.getReturnedObjectType()).thenReturn(returnedObjectClass);
		if (hashKeyProperty != null) {
			Mockito.when(mockEntityMetadata.isHashKeyProperty(hashKeyProperty)).thenReturn(true);
		}
		
		for (int i = 0; i < numberOfParameters; i++) {
			Parameter mockParameter = Mockito.mock(Parameter.class);
			Mockito.when(mockParameter.getIndex()).thenReturn(i);
			Mockito.when(mockParameters.getBindableParameter(i)).thenReturn(mockParameter);
		}
		partTreeDynamoDBQuery = new PartTreeDynamoDBQuery(mockDynamoDBMapper, mockDynamoDBQueryMethod);
	}

	@Test
	public void testGetQueryMethod() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findById", 1, "id", null);
		assertEquals(mockDynamoDBUserQueryMethod, partTreeDynamoDBQuery.getQueryMethod());
	}

	@Test
	public void testExecute_WhenFinderMethodIsFindingSingleEntity_WithSingleStringParameter_WhenFindingByHashKey() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findById", 1, "id", null);

		// Mock out specific DynamoDBMapper behavior expected by this method
		Mockito.when(mockDynamoDBMapper.load(User.class, "someId")).thenReturn(mockUser);

		// Execute the query
		Object[] parameters = new Object[] { "someId" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected single result
		assertEquals(o, mockUser);

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).load(User.class, "someId");
	}

	@Test
	public void testExecute_WhenFinderMethodIsFindingSingleEntityWithCompositeId_WithSingleStringParameter_WhenFindingByHashAndRangeKey() {
		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserNameAndPlaylistName", 2, "userName", "playlistName");

		// Mock out specific DynamoDBMapper behavior expected by this method
		Mockito.when(mockDynamoDBMapper.load(Playlist.class, "someUserName", "somePlaylistName")).thenReturn(
				mockPlaylist);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName", "somePlaylistName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected single result
		assertEquals(o, mockPlaylist);

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).load(Playlist.class, "someUserName", "somePlaylistName");
	}

	@Test
	public void testExecute_WhenFinderMethodIsFindingSingleEntityWithCompositeId_WhenFindingByCompositeId() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUserName");
		playlistId.setPlaylistName("somePlaylistName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistId", 1, "userName", "playlistName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn("somePlaylistName");

		// Mock out specific DynamoDBMapper behavior expected by this method
		Mockito.when(mockDynamoDBMapper.load(Playlist.class, "someUserName", "somePlaylistName")).thenReturn(
				mockPlaylist);

		// Execute the query

		Object[] parameters = new Object[] { playlistId };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected single result
		assertEquals(o, mockPlaylist);

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).load(Playlist.class, "someUserName", "somePlaylistName");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByNotCompositeId() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUserName");
		playlistId.setPlaylistName("somePlaylistName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistIdNot", 1, "userName", "playlistName");

		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);

		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn("somePlaylistName");

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistScanResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockPlaylistScanResults);

		// Execute the query

		Object[] parameters = new Object[] { playlistId };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistScanResults, o);
		assertEquals(1, mockPlaylistScanResults.size());
		assertEquals(mockPlaylist, mockPlaylistScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we only one filter condition for each of the two
		// properties
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(2, filterConditions.size());
		Condition filterCondition1 = filterConditions.get("userName");
		Condition filterCondition2 = filterConditions.get("playlistName");

		assertNotNull(filterCondition1);
		assertNotNull(filterCondition2);

		assertEquals(ComparisonOperator.NE.name(), filterCondition1.getComparisonOperator());
		assertEquals(ComparisonOperator.NE.name(), filterCondition2.getComparisonOperator());

		// Assert we only have one attribute value for each filter condition
		assertEquals(1, filterCondition1.getAttributeValueList().size());
		assertEquals(1, filterCondition2.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someUserName", filterCondition1.getAttributeValueList().get(0).getS());
		assertEquals("somePlaylistName", filterCondition2.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition1.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getN());
		assertNull(filterCondition1.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getB());
		assertNull(filterCondition1.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByRangeKeyOnly() {

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistName", 1, "userName", "playlistName");

		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistScanResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockPlaylistScanResults);

		// Execute the query

		Object[] parameters = new Object[] { "somePlaylistName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistScanResults, o);
		assertEquals(1, mockPlaylistScanResults.size());
		assertEquals(mockPlaylist, mockPlaylistScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we only one filter condition for the one property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("playlistName");

		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for the filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("somePlaylistName", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByCompositeIdWithRangeKeyOnly() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setPlaylistName("somePlaylistName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistId", 1, "userName", "playlistName");

		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn(null);
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn("somePlaylistName");

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistScanResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockPlaylistScanResults);

		// Execute the query

		Object[] parameters = new Object[] { playlistId };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistScanResults, o);
		assertEquals(1, mockPlaylistScanResults.size());
		assertEquals(mockPlaylist, mockPlaylistScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we only one filter condition for the one property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("playlistName");

		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for the filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("somePlaylistName", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WithSingleStringParameter_WhenFindingByHashKeyOnly() {
		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserName", 1, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistQueryResults, o);
		assertEquals(1, mockPlaylistQueryResults.size());
		assertEquals(mockPlaylist, mockPlaylistQueryResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Object hashKeyPrototypeObject = queryCaptor.getValue().getHashKeyValues();
		assertTrue(hashKeyPrototypeObject instanceof Playlist);
		Playlist hashKeyPropertyPlaylist = (Playlist) hashKeyPrototypeObject;
		assertEquals("someUserName", hashKeyPropertyPlaylist.getUserName());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).query(classCaptor.getValue(), queryCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WithSingleStringParameter_WhenFindingByHashKeyAndNotRangeKey() {
		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserNameAndPlaylistNameNot", 2, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");

		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName", "somePlaylistName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistQueryResults, o);
		assertEquals(1, mockPlaylistQueryResults.size());
		assertEquals(mockPlaylist, mockPlaylistQueryResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have a query key condition and a filter condition
		Object hashKeyPrototypeObject = queryCaptor.getValue().getHashKeyValues();
		assertTrue(hashKeyPrototypeObject instanceof Playlist);
		Playlist hashKeyPropertyPlaylist = (Playlist) hashKeyPrototypeObject;
		assertEquals("someUserName", hashKeyPropertyPlaylist.getUserName());
		// Assert that we only one filter condition for the one property
		Map<String, Condition> filterConditions = queryCaptor.getValue().getRangeKeyConditions();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("playlistName");

		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.NE.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for the filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("somePlaylistName", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).query(classCaptor.getValue(), queryCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByCompositeIdWithHashKeyOnly() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUserName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistId", 1, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn(null);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { playlistId };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistQueryResults, o);
		assertEquals(1, mockPlaylistQueryResults.size());
		assertEquals(mockPlaylist, mockPlaylistQueryResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Object hashKeyPrototypeObject = queryCaptor.getValue().getHashKeyValues();
		assertTrue(hashKeyPrototypeObject instanceof Playlist);
		Playlist hashKeyPropertyPlaylist = (Playlist) hashKeyPrototypeObject;
		assertEquals("someUserName", hashKeyPropertyPlaylist.getUserName());
		
		
		assertEquals(0,queryCaptor.getValue().getRangeKeyConditions().size());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).query(classCaptor.getValue(), queryCaptor.getValue());

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByCompositeIdWithHashKeyOnly_WhenSortingByRangeKey() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUserName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistIdOrderByPlaylistNameDesc", 1, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn(null);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { playlistId };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistQueryResults, o);
		assertEquals(1, mockPlaylistQueryResults.size());
		assertEquals(mockPlaylist, mockPlaylistQueryResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Object hashKeyPrototypeObject = queryCaptor.getValue().getHashKeyValues();
		assertTrue(hashKeyPrototypeObject instanceof Playlist);
		Playlist hashKeyPropertyPlaylist = (Playlist) hashKeyPrototypeObject;
		assertEquals("someUserName", hashKeyPropertyPlaylist.getUserName());
		
		
		assertEquals(0,queryCaptor.getValue().getRangeKeyConditions().size());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).query(classCaptor.getValue(), queryCaptor.getValue());

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	// Can't sort by indexrangekey when querying by hash key only
	@Test(expected=UnsupportedOperationException.class)
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByCompositeIdWithHashKeyOnly_WhenSortingByIndexRangeKey() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUserName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistIdOrderByDisplayNameDesc", 1, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn(null);
		Set<String> indexRangeKeyPropertyNames = new HashSet<String>();
		indexRangeKeyPropertyNames.add("displayName");
		Mockito.when(mockPlaylistEntityMetadata.getIndexRangeKeyPropertyNames()).thenReturn(indexRangeKeyPropertyNames);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { playlistId };
		partTreeDynamoDBQuery.execute(parameters);

		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByHashKeyAndIndexRangeKey() {
		

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserNameAndDisplayName", 2, "userName", "playlistName");
		Set<String> indexRangeKeyPropertyNames = new HashSet<String>();
		indexRangeKeyPropertyNames.add("displayName");
		Mockito.when(mockPlaylistEntityMetadata.getIndexRangeKeyPropertyNames()).thenReturn(indexRangeKeyPropertyNames);
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName","someDisplayName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistQueryResults, o);
		assertEquals(1, mockPlaylistQueryResults.size());
		assertEquals(mockPlaylist, mockPlaylistQueryResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only one filter condition for the hash key,and for the index range key
		Object hashKeyPrototypeObject = queryCaptor.getValue().getHashKeyValues();
		assertTrue(hashKeyPrototypeObject instanceof Playlist);
		Playlist hashKeyPropertyPlaylist = (Playlist) hashKeyPrototypeObject;
		assertEquals("someUserName", hashKeyPropertyPlaylist.getUserName());
		
		assertEquals(1,queryCaptor.getValue().getRangeKeyConditions().size());
		Condition condition = (Condition) queryCaptor.getValue().getRangeKeyConditions().get("displayName");
		assertEquals(ComparisonOperator.EQ.name(),condition.getComparisonOperator());
		assertEquals(1,condition.getAttributeValueList().size());
		assertEquals("someDisplayName",condition.getAttributeValueList().get(0).getS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).query(classCaptor.getValue(), queryCaptor.getValue());

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByHashKeyAndIndexRangeKey_OrderByIndexRangeKey() {
		

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserNameAndDisplayNameOrderByDisplayNameDesc", 2, "userName", "playlistName");
		Set<String> indexRangeKeyPropertyNames = new HashSet<String>();
		indexRangeKeyPropertyNames.add("displayName");
		Mockito.when(mockPlaylistEntityMetadata.getIndexRangeKeyPropertyNames()).thenReturn(indexRangeKeyPropertyNames);
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName","someDisplayName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistQueryResults, o);
		assertEquals(1, mockPlaylistQueryResults.size());
		assertEquals(mockPlaylist, mockPlaylistQueryResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only one filter condition for the hash key,and for the index range key
		Object hashKeyPrototypeObject = queryCaptor.getValue().getHashKeyValues();
		assertTrue(hashKeyPrototypeObject instanceof Playlist);
		Playlist hashKeyPropertyPlaylist = (Playlist) hashKeyPrototypeObject;
		assertEquals("someUserName", hashKeyPropertyPlaylist.getUserName());
		
		assertEquals(1,queryCaptor.getValue().getRangeKeyConditions().size());
		Condition condition = (Condition) queryCaptor.getValue().getRangeKeyConditions().get("displayName");
		assertEquals(ComparisonOperator.EQ.name(),condition.getComparisonOperator());
		assertEquals(1,condition.getAttributeValueList().size());
		assertEquals("someDisplayName",condition.getAttributeValueList().get(0).getS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).query(classCaptor.getValue(), queryCaptor.getValue());

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	// Sorting by range key when querying by indexrangekey not supported
	@Test(expected=UnsupportedOperationException.class)
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByHashKeyAndIndexRangeKey_OrderByRangeKey() {
		

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserNameAndDisplayNameOrderByPlaylistNameDesc", 2, "userName", "playlistName");
		Set<String> indexRangeKeyPropertyNames = new HashSet<String>();
		indexRangeKeyPropertyNames.add("displayName");
		Mockito.when(mockPlaylistEntityMetadata.getIndexRangeKeyPropertyNames()).thenReturn(indexRangeKeyPropertyNames);
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName","someDisplayName" };
		partTreeDynamoDBQuery.execute(parameters);


	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByHashKeyAndIndexRangeKeyWithOveriddenName() {
		

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserNameAndDisplayName", 2, "userName", "playlistName");
		Set<String> indexRangeKeyPropertyNames = new HashSet<String>();
		indexRangeKeyPropertyNames.add("displayName");
		Mockito.when(mockPlaylistEntityMetadata.getIndexRangeKeyPropertyNames()).thenReturn(indexRangeKeyPropertyNames);
		Mockito.when(mockPlaylistEntityMetadata.getOverriddenAttributeName("displayName")).thenReturn("DisplayName");

		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBQueryExpression> queryCaptor = ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockPlaylistQueryResults.get(0)).thenReturn(mockPlaylist);
		Mockito.when(mockPlaylistQueryResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.query(classCaptor.capture(), queryCaptor.capture())).thenReturn(
				mockPlaylistQueryResults);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName","someDisplayName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected results
		assertEquals(mockPlaylistQueryResults, o);
		assertEquals(1, mockPlaylistQueryResults.size());
		assertEquals(mockPlaylist, mockPlaylistQueryResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only one filter condition for the hash key,and for the index range key
		Object hashKeyPrototypeObject = queryCaptor.getValue().getHashKeyValues();
		assertTrue(hashKeyPrototypeObject instanceof Playlist);
		Playlist hashKeyPropertyPlaylist = (Playlist) hashKeyPrototypeObject;
		assertEquals("someUserName", hashKeyPropertyPlaylist.getUserName());
		
		assertEquals(1,queryCaptor.getValue().getRangeKeyConditions().size());
		Condition condition = (Condition) queryCaptor.getValue().getRangeKeyConditions().get("DisplayName");
		assertEquals(ComparisonOperator.EQ.name(),condition.getComparisonOperator());
		assertEquals(1,condition.getAttributeValueList().size());
		assertEquals("someDisplayName",condition.getAttributeValueList().get(0).getS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).query(classCaptor.getValue(), queryCaptor.getValue());

	}
	
	
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByCompositeIdWithHashKeyOnlyAndByAnotherPropertyWithOverriddenAttributeName() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUserName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistIdAndDisplayName", 2, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn(null);
		Mockito.when(mockPlaylistEntityMetadata.getOverriddenAttributeName("displayName")).thenReturn("DisplayName");

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { playlistId, "someDisplayName" };
		partTreeDynamoDBQuery.execute(parameters);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only three filter conditions
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(2, filterConditions.size());
		Condition filterCondition1 = filterConditions.get("userName");
		Condition filterCondition2 = filterConditions.get("DisplayName");

		assertEquals(ComparisonOperator.EQ.name(), filterCondition1.getComparisonOperator());
		assertEquals(ComparisonOperator.EQ.name(), filterCondition2.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition1.getAttributeValueList().size());
		assertEquals(1, filterCondition2.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someUserName", filterCondition1.getAttributeValueList().get(0).getS());
		assertEquals("someDisplayName", filterCondition2.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition1.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getN());
		assertNull(filterCondition1.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getB());
		assertNull(filterCondition1.getAttributeValueList().get(0).getBS());

		assertNull(filterCondition2.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getN());
		assertNull(filterCondition2.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getB());
		assertNull(filterCondition2.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue()); // Assert
																								// that
																								// we
																								// obtain
																								// the
																								// expected
																								// results

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByCompositeIdWithRangeKeyOnlyAndByAnotherPropertyWithOverriddenAttributeName() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setPlaylistName("somePlaylistName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistIdAndDisplayName", 2, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn(null);
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn("somePlaylistName");
		Mockito.when(mockPlaylistEntityMetadata.getOverriddenAttributeName("displayName")).thenReturn("DisplayName");

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { playlistId, "someDisplayName" };
		partTreeDynamoDBQuery.execute(parameters);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only three filter conditions
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(2, filterConditions.size());
		Condition filterCondition1 = filterConditions.get("playlistName");
		Condition filterCondition2 = filterConditions.get("DisplayName");

		assertEquals(ComparisonOperator.EQ.name(), filterCondition1.getComparisonOperator());
		assertEquals(ComparisonOperator.EQ.name(), filterCondition2.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition1.getAttributeValueList().size());
		assertEquals(1, filterCondition2.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("somePlaylistName", filterCondition1.getAttributeValueList().get(0).getS());
		assertEquals("someDisplayName", filterCondition2.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition1.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getN());
		assertNull(filterCondition1.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getB());
		assertNull(filterCondition1.getAttributeValueList().get(0).getBS());

		assertNull(filterCondition2.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getN());
		assertNull(filterCondition2.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getB());
		assertNull(filterCondition2.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue()); // Assert
																								// that
																								// we
																								// obtain
																								// the
																								// expected
																								// results

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByNotHashKeyAndNotRangeKey() {

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByUserNameNotAndPlaylistNameNot", 2, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someUserName", "somePlaylistName" };
		partTreeDynamoDBQuery.execute(parameters);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only three filter conditions
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(2, filterConditions.size());
		Condition filterCondition1 = filterConditions.get("userName");
		Condition filterCondition2 = filterConditions.get("playlistName");

		assertEquals(ComparisonOperator.NE.name(), filterCondition1.getComparisonOperator());
		assertEquals(ComparisonOperator.NE.name(), filterCondition2.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition1.getAttributeValueList().size());
		assertEquals(1, filterCondition2.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someUserName", filterCondition1.getAttributeValueList().get(0).getS());
		assertEquals("somePlaylistName", filterCondition2.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition1.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getN());
		assertNull(filterCondition1.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getB());
		assertNull(filterCondition1.getAttributeValueList().get(0).getBS());

		assertNull(filterCondition2.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getN());
		assertNull(filterCondition2.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getB());
		assertNull(filterCondition2.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue()); // Assert
																								// that
																								// we
																								// obtain
																								// the
																								// expected
																								// results

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityWithCompositeIdList_WhenFindingByCompositeIdAndByAnotherPropertyWithOverriddenAttributeName() {
		PlaylistId playlistId = new PlaylistId();
		playlistId.setUserName("someUserName");
		playlistId.setPlaylistName("somePlaylistName");

		setupCommonMocksForThisRepositoryMethod(mockPlaylistEntityMetadata, mockDynamoDBPlaylistQueryMethod,
				Playlist.class, "findByPlaylistIdAndDisplayName", 2, "userName", "playlistName");
		Mockito.when(mockDynamoDBPlaylistQueryMethod.isCollectionQuery()).thenReturn(true);
		Playlist prototypeHashKey = new Playlist();
		prototypeHashKey.setUserName("someUserName");

		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("someUserName")).thenReturn(
				prototypeHashKey);
		Mockito.when(mockPlaylistEntityMetadata.getHashKey(playlistId)).thenReturn("someUserName");
		Mockito.when(mockPlaylistEntityMetadata.getRangeKey(playlistId)).thenReturn("somePlaylistName");
		Mockito.when(mockPlaylistEntityMetadata.getOverriddenAttributeName("displayName")).thenReturn("DisplayName");

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { playlistId, "someDisplayName" };
		partTreeDynamoDBQuery.execute(parameters);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), Playlist.class);

		// Assert that we have only three filter conditions
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(3, filterConditions.size());
		Condition filterCondition1 = filterConditions.get("userName");
		Condition filterCondition2 = filterConditions.get("playlistName");
		Condition filterCondition3 = filterConditions.get("DisplayName");

		assertEquals(ComparisonOperator.EQ.name(), filterCondition1.getComparisonOperator());
		assertEquals(ComparisonOperator.EQ.name(), filterCondition2.getComparisonOperator());
		assertEquals(ComparisonOperator.EQ.name(), filterCondition3.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition1.getAttributeValueList().size());
		assertEquals(1, filterCondition2.getAttributeValueList().size());
		assertEquals(1, filterCondition3.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someUserName", filterCondition1.getAttributeValueList().get(0).getS());
		assertEquals("somePlaylistName", filterCondition2.getAttributeValueList().get(0).getS());
		assertEquals("someDisplayName", filterCondition3.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition1.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getN());
		assertNull(filterCondition1.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition1.getAttributeValueList().get(0).getB());
		assertNull(filterCondition1.getAttributeValueList().get(0).getBS());

		assertNull(filterCondition2.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getN());
		assertNull(filterCondition2.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition2.getAttributeValueList().get(0).getB());
		assertNull(filterCondition2.getAttributeValueList().get(0).getBS());

		assertNull(filterCondition3.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition3.getAttributeValueList().get(0).getN());
		assertNull(filterCondition3.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition3.getAttributeValueList().get(0).getB());
		assertNull(filterCondition3.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue()); // Assert
																								// that
																								// we
																								// obtain
																								// the
																								// expected
																								// results

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingSingleEntity_WithSingleStringParameter_WhenNotFindingByHashKey() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByName", 1, "id", null);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected single result
		assertEquals(o, mockUser);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("name");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someName", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingSingleEntity_WithSingleStringParameter_WhenNotFindingByHashKey_WhenDynamoAttributeNameOverridden() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByName", 1, "id", null);
		Mockito.when(mockUserEntityMetadata.getOverriddenAttributeName("name")).thenReturn("Name");

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected single result
		assertEquals(o, mockUser);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("Name");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someName", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingSingleEntity_WithMultipleStringParameters_WhenFindingByHashKeyAndANonHashOrRangeProperty() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByIdAndName", 2, "id", null);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someId", "someName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected single result
		assertEquals(o, mockUser);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have two filter conditions, for the id and name
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(2, filterConditions.size());
		Condition nameFilterCondition = filterConditions.get("name");
		assertNotNull(nameFilterCondition);
		Condition idFilterCondition = filterConditions.get("id");
		assertNotNull(idFilterCondition);

		assertEquals(ComparisonOperator.EQ.name(), nameFilterCondition.getComparisonOperator());
		assertEquals(ComparisonOperator.EQ.name(), idFilterCondition.getComparisonOperator());

		// Assert we only have one attribute value for each filter condition
		assertEquals(1, nameFilterCondition.getAttributeValueList().size());
		assertEquals(1, idFilterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someName", nameFilterCondition.getAttributeValueList().get(0).getS());
		assertEquals("someId", idFilterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getSS());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getN());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getNS());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getB());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getBS());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getSS());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getN());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getNS());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getB());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}
	
	
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingSingleEntity_WithMultipleStringParameters_WhenFindingByHashKeyAndANonHashOrRangeProperty_WhenDynamoDBAttributeNamesOveridden() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByIdAndName", 2, "id", null);

		Mockito.when(mockUserEntityMetadata.getOverriddenAttributeName("name")).thenReturn("Name");
		Mockito.when(mockUserEntityMetadata.getOverriddenAttributeName("id")).thenReturn("Id");

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someId", "someName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected single result
		assertEquals(o, mockUser);

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have two filter conditions, for the id and name
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(2, filterConditions.size());
		Condition nameFilterCondition = filterConditions.get("Name");
		assertNotNull(nameFilterCondition);
		Condition idFilterCondition = filterConditions.get("Id");
		assertNotNull(idFilterCondition);

		assertEquals(ComparisonOperator.EQ.name(), nameFilterCondition.getComparisonOperator());
		assertEquals(ComparisonOperator.EQ.name(), idFilterCondition.getComparisonOperator());

		// Assert we only have one attribute value for each filter condition
		assertEquals(1, nameFilterCondition.getAttributeValueList().size());
		assertEquals(1, idFilterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someName", nameFilterCondition.getAttributeValueList().get(0).getS());
		assertEquals("someId", idFilterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getSS());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getN());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getNS());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getB());
		assertNull(nameFilterCondition.getAttributeValueList().get(0).getBS());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getSS());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getN());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getNS());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getB());
		assertNull(idFilterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleStringParameter_WhenNotFindingByHashKey() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByName", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected list of results
		assertEquals(o, mockUserScanResults);

		// Assert that the list of results contains the correct elements
		assertEquals(1, mockUserScanResults.size());
		assertEquals(mockUser, mockUserScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("name");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someName", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = UnsupportedOperationException.class)
	// Not yet supported
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleStringParameterIgnoringCase_WhenNotFindingByHashKey() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByNameIgnoringCase", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someName" };
		partTreeDynamoDBQuery.execute(parameters);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = UnsupportedOperationException.class)
	// Not yet supported
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleStringParameter_WithSort_WhenNotFindingByHashKey() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByNameOrderByNameAsc", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someName" };
		partTreeDynamoDBQuery.execute(parameters);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = IllegalArgumentException.class)
	// Not yet supported
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleStringParameter_WithIn_WhenNotFindingByHashKey() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByNameIn", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someName" };
		partTreeDynamoDBQuery.execute(parameters);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleDateParameter_WhenNotFindingByHashKey()
			throws ParseException {
		String joinDateString = "2013-09-12T14:04:03.123Z";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date joinDate = dateFormat.parse(joinDateString);

		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByJoinDate", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);
		
		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { joinDate };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected list of results
		assertEquals(o, mockUserScanResults);

		// Assert that the list of results contains the correct elements
		assertEquals(1, mockUserScanResults.size());
		assertEquals(mockUser, mockUserScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("joinDate");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals(joinDateString, filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleDateParameter_WithCustomMarshaller_WhenNotFindingByHashKey()
			throws ParseException {
		String joinYearString = "2013";
		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date joinYear = dateFormat.parse(joinYearString);

		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByJoinYear", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);
		DynamoDBMarshaller marshaller = new DynamoDBYearMarshaller();

		Mockito.when(mockUserEntityMetadata.getMarshallerForProperty("joinYear")).thenReturn(marshaller);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { joinYear };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected list of results
		assertEquals(o, mockUserScanResults);

		// Assert that the list of results contains the correct elements
		assertEquals(1, mockUserScanResults.size());
		assertEquals(mockUser, mockUserScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("joinYear");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals(joinYearString, filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleStringParameter_WithCustomMarshaller_WhenNotFindingByHashKey()
			throws ParseException {

		String postcode = "N1";

		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByPostCode", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);
		DynamoDBMarshaller marshaller = new CaseChangingMarshaller();

		Mockito.when(mockUserEntityMetadata.getMarshallerForProperty("postCode")).thenReturn(marshaller);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { postcode };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected list of results
		assertEquals(o, mockUserScanResults);

		// Assert that the list of results contains the correct elements
		assertEquals(1, mockUserScanResults.size());
		assertEquals(mockUser, mockUserScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("postCode");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("n1", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleIntegerParameter_WhenNotFindingByHashKey() {
		int numberOfPlaylists = 5;

		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByNumberOfPlaylists", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { numberOfPlaylists };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected list of results
		assertEquals(o, mockUserScanResults);

		// Assert that the list of results contains the correct elements
		assertEquals(1, mockUserScanResults.size());
		assertEquals(mockUser, mockUserScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("numberOfPlaylists");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.EQ.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is Number,
		// and its Dynamo value is the number as a string
		assertEquals("5", filterCondition.getAttributeValueList().get(0).getN());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getS());
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleStringParameter_WhenNotFindingByNotHashKey() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByIdNot", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someId" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected list of results
		assertEquals(o, mockUserScanResults);

		// Assert that the list of results contains the correct elements
		assertEquals(1, mockUserScanResults.size());
		assertEquals(mockUser, mockUserScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("id");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.NE.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someId", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testExecute_WhenFinderMethodIsFindingEntityList_WithSingleStringParameter_WhenNotFindingByNotAProperty() {
		setupCommonMocksForThisRepositoryMethod(mockUserEntityMetadata, mockDynamoDBUserQueryMethod, User.class,
				"findByNameNot", 1, "id", null);
		Mockito.when(mockDynamoDBUserQueryMethod.isCollectionQuery()).thenReturn(true);

		// Mock out specific DynamoDBMapper behavior expected by this method
		ArgumentCaptor<DynamoDBScanExpression> scanCaptor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
		ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
		Mockito.when(mockUserScanResults.get(0)).thenReturn(mockUser);
		Mockito.when(mockUserScanResults.size()).thenReturn(1);
		Mockito.when(mockDynamoDBMapper.scan(classCaptor.capture(), scanCaptor.capture())).thenReturn(
				mockUserScanResults);

		// Execute the query
		Object[] parameters = new Object[] { "someName" };
		Object o = partTreeDynamoDBQuery.execute(parameters);

		// Assert that we obtain the expected list of results
		assertEquals(o, mockUserScanResults);

		// Assert that the list of results contains the correct elements
		assertEquals(1, mockUserScanResults.size());
		assertEquals(mockUser, mockUserScanResults.get(0));

		// Assert that we scanned DynamoDB for the correct class
		assertEquals(classCaptor.getValue(), User.class);

		// Assert that we have only one filter condition, for the name of the
		// property
		Map<String, Condition> filterConditions = scanCaptor.getValue().getScanFilter();
		assertEquals(1, filterConditions.size());
		Condition filterCondition = filterConditions.get("name");
		assertNotNull(filterCondition);

		assertEquals(ComparisonOperator.NE.name(), filterCondition.getComparisonOperator());

		// Assert we only have one attribute value for this filter condition
		assertEquals(1, filterCondition.getAttributeValueList().size());

		// Assert that there the attribute value type for this attribute value
		// is String,
		// and its value is the parameter expected
		assertEquals("someName", filterCondition.getAttributeValueList().get(0).getS());

		// Assert that all other attribute value types other than String type
		// are null
		assertNull(filterCondition.getAttributeValueList().get(0).getSS());
		assertNull(filterCondition.getAttributeValueList().get(0).getN());
		assertNull(filterCondition.getAttributeValueList().get(0).getNS());
		assertNull(filterCondition.getAttributeValueList().get(0).getB());
		assertNull(filterCondition.getAttributeValueList().get(0).getBS());

		// Verify that the expected DynamoDBMapper method was called
		Mockito.verify(mockDynamoDBMapper).scan(classCaptor.getValue(), scanCaptor.getValue());
	}

}
