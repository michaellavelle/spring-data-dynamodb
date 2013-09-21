package org.socialsignin.spring.data.dynamodb.repository.support;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.PlaylistId;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unused")
public class DynamoDBIdIsHashKeyEntityInformationImplUnitTest {
	
	private DynamoDBIdIsHashKeyEntityInformationImpl<Playlist,PlaylistId> dynamoDBPlaylistEntityInformation;
	
	private DynamoDBIdIsHashKeyEntityInformationImpl<User,String> dynamoDBUserEntityInformation;
	
	@Mock
	private DynamoDBHashAndRangeKeyExtractingEntityMetadata<Playlist,String> mockPlaylistEntityMetadata;
	
	@Mock
	private DynamoDBHashKeyExtractingEntityMetadata<User> mockUserEntityMetadata;

	@Mock
	private Object mockHashKey;
	
	@Mock
	private User mockUserPrototype;
	
	@Mock
	private Playlist mockPlaylistPrototype;
	
	@SuppressWarnings("rawtypes")
	@Mock
	private DynamoDBMarshaller mockPropertyMarshaller;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup()
	{

		Mockito.when(mockUserEntityMetadata.getHashKeyPropertyName()).thenReturn("userHashKeyPropertyName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropertyName()).thenReturn("playlistHashKeyPropertyName");
		Mockito.when(mockPlaylistEntityMetadata.getHashKeyPropotypeEntityForHashKey("somePlaylistHashKey")).thenReturn(mockPlaylistPrototype);
		Mockito.when(mockUserEntityMetadata.getMarshallerForProperty("marshalledProperty")).thenReturn(mockPropertyMarshaller);
		Mockito.when(mockPlaylistEntityMetadata.getMarshallerForProperty("marshalledProperty")).thenReturn(mockPropertyMarshaller);
		Mockito.when(mockUserEntityMetadata.getOverriddenAttributeName("overriddenProperty")).thenReturn("modifiedPropertyName");
		Mockito.when(mockPlaylistEntityMetadata.getOverriddenAttributeName("overriddenProperty")).thenReturn("modifiedPropertyName");

		Mockito.when(mockUserEntityMetadata.isHashKeyProperty("hashKeyProperty")).thenReturn(true);
		Mockito.when(mockPlaylistEntityMetadata.isHashKeyProperty("hashKeyProperty")).thenReturn(true);
		Mockito.when(mockUserEntityMetadata.isHashKeyProperty("nonHashKeyProperty")).thenReturn(false);
		Mockito.when(mockPlaylistEntityMetadata.isHashKeyProperty("nonHashKeyProperty")).thenReturn(false);
		Mockito.when(mockPlaylistEntityMetadata.isCompositeHashAndRangeKeyProperty("compositeIdProperty")).thenReturn(true);
		Mockito.when(mockPlaylistEntityMetadata.isCompositeHashAndRangeKeyProperty("nonCompositeIdProperty")).thenReturn(false);
		
		dynamoDBPlaylistEntityInformation = new DynamoDBIdIsHashKeyEntityInformationImpl<Playlist,PlaylistId>(Playlist.class,mockPlaylistEntityMetadata);
		dynamoDBUserEntityInformation = new DynamoDBIdIsHashKeyEntityInformationImpl<User,String>(User.class,mockUserEntityMetadata);

	}
	
	@Test
	public void testGetId_WhenHashKeyTypeSameAsIdType_InvokesHashKeyMethod_AndReturnedIdIsAssignableToIdType_AndIsValueExpected()
	{
		User user = new User();
		user.setId("someUserId");
		String id = dynamoDBUserEntityInformation.getId(user);
		Assert.assertEquals("someUserId", id);

	}
	
	@Test(expected=ClassCastException.class)
	public void testGetId_WhenHashKeyMethodNotSameAsIdType_InvokesHashKeyMethod_AndReturnedIdIsNotAssignableToIdType()
	{
		Playlist playlist = new Playlist();
		playlist.setUserName("someUserName");
		playlist.setPlaylistName("somePlaylistName");
		PlaylistId id = dynamoDBPlaylistEntityInformation.getId(playlist);
	}
	
	@Test
	public void testGetHashKeyGivenId_WhenHashKeyTypeSameAsIdType_ReturnsId()
	{
		Object hashKey = dynamoDBUserEntityInformation.getHashKey("someUserId");	
		Assert.assertNotNull(hashKey);
		Assert.assertEquals("someUserId",hashKey);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetHashKeyGivenId_WhenHashKeyTypeNotSameAsIdType_ThrowsIllegalArgumentException()
	{
		PlaylistId id = new PlaylistId();
		Object hashKey = dynamoDBPlaylistEntityInformation.getHashKey(id);	
		Assert.assertNotNull(hashKey);
		Assert.assertEquals(id,hashKey);
	}
	
	@Test
	public void testGetJavaType_WhenEntityIsInstanceWithHashAndRangeKey_ReturnsEntityClass()
	{
		Assert.assertEquals(Playlist.class,dynamoDBPlaylistEntityInformation.getJavaType());
	}
	
	@Test
	public void testGetJavaType_WhenEntityIsInstanceWithHashKeyOnly_ReturnsEntityClass()
	{
		Assert.assertEquals(User.class,dynamoDBUserEntityInformation.getJavaType());
	}
	
	@Test
	public void testGetIdType_WhenEntityIsInstanceWithHashAndRangeKey_ReturnsReturnTypeOfHashKeyMethod()
	{
		Assert.assertEquals(String.class,dynamoDBPlaylistEntityInformation.getIdType());
	}
	
	@Test
	public void testGetIdType_WhenEntityIsInstanceWithHashKeyOnly_ReturnsReturnTypeOfHashKeyMethod()
	{
		Assert.assertEquals(String.class,dynamoDBUserEntityInformation.getIdType());
	}
	
	
	// The following tests ensure that invarient methods such as those always retuning constants, or
	// that delegate to metadata, behave the same irrespective of the setup of the EntityInformation
	
	
	@Test
	public void testGetRangeKey_ReturnsNull_IrrespectiveOfEntityInformationSetup()
	{
		Object userRangeKey = dynamoDBUserEntityInformation.getRangeKey("someUserId");	
		Assert.assertNull(userRangeKey);
		
		Object playlistRangeKey = dynamoDBPlaylistEntityInformation.getRangeKey(new PlaylistId());	
		Assert.assertNull(playlistRangeKey);
	}
	
	@Test
	public void testIsRangeKeyAware_ReturnsFalse_IrrespectiveOfEntityInformationSetup()
	{
		Assert.assertFalse(dynamoDBUserEntityInformation.isRangeKeyAware());	
		
		Assert.assertFalse(dynamoDBPlaylistEntityInformation.isRangeKeyAware());	
	}
	
	
	@Test
	public void testGetHashKeyPropertyName_DelegatesToEntityMetadata_IrrespectiveOfEntityInformationSetup()
	{
		Assert.assertEquals("userHashKeyPropertyName", dynamoDBUserEntityInformation.getHashKeyPropertyName());
		Assert.assertEquals("playlistHashKeyPropertyName", dynamoDBPlaylistEntityInformation.getHashKeyPropertyName());

	}

	@Test
	public void testGetMarshallerForProperty_DelegatesToEntityMetadata_IrrespectiveOfEntityInformationSetup()
	{
		DynamoDBMarshaller<?> marshaller1 =  dynamoDBPlaylistEntityInformation.getMarshallerForProperty("marshalledProperty");
		Assert.assertEquals(mockPropertyMarshaller, marshaller1);
		
		DynamoDBMarshaller<?> marshaller2 =  dynamoDBUserEntityInformation.getMarshallerForProperty("marshalledProperty");
		Assert.assertEquals(mockPropertyMarshaller, marshaller2);
	}
	
	@Test
	public void testGetIsHashKeyProperty_DelegatesToEntityMetadata_IrrespectiveOfEntityInformationSetup()
	{
		Assert.assertTrue(dynamoDBUserEntityInformation.isHashKeyProperty("hashKeyProperty"));
		Assert.assertTrue(dynamoDBUserEntityInformation.isHashKeyProperty("hashKeyProperty"));
		
		Assert.assertFalse(dynamoDBPlaylistEntityInformation.isHashKeyProperty("nonHashKeyProperty"));
		Assert.assertFalse(dynamoDBPlaylistEntityInformation.isHashKeyProperty("nonHashKeyProperty"));
	}
	
	@Test
	public void testGetIsCompositeIdProperty_ReturnsFalse_IrrespectiveOfEntityInformationSetup()
	{
		Assert.assertFalse(dynamoDBUserEntityInformation.isCompositeHashAndRangeKeyProperty("compositeIdProperty"));
		Assert.assertFalse(dynamoDBUserEntityInformation.isCompositeHashAndRangeKeyProperty("compositeIdProperty"));
		
		Assert.assertFalse(dynamoDBPlaylistEntityInformation.isCompositeHashAndRangeKeyProperty("nonCompositeIdProperty"));
		Assert.assertFalse(dynamoDBPlaylistEntityInformation.isCompositeHashAndRangeKeyProperty("nonCompositeIdProperty"));
	}
	
	@Test
	public void testGetOverriddenAttributeName_DelegatesToEntityMetadata_IrrespectiveOfEntityInformationSetup()
	{
		String propertyName1 =  dynamoDBUserEntityInformation.getOverriddenAttributeName("overriddenProperty");
		Assert.assertEquals("modifiedPropertyName", propertyName1);
		
		String propertyName2 =  dynamoDBPlaylistEntityInformation.getOverriddenAttributeName("overriddenProperty");
		Assert.assertEquals("modifiedPropertyName", propertyName2);
	}

	
}
