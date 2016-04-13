package org.socialsignin.spring.data.dynamodb.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameResolver;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBTemplateUnitTest {
	
	@Mock
	private DynamoDBMapper dynamoDBMapper;
	
	@Mock
	private AmazonDynamoDB dynamoDB;
	
	private DynamoDBTemplate dynamoDBTemplate;
	
	@Before
	public void setUp() {
		
		this.dynamoDBTemplate = new DynamoDBTemplate(dynamoDB);
		this.dynamoDBTemplate.dynamoDBMapper = dynamoDBMapper;
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBatchDelete_CallsCorrectDynamoDBMapperMethod()
	{
			List<User> users = new ArrayList<User>();
			dynamoDBTemplate.batchDelete(users);
			Mockito.verify(dynamoDBMapper).batchDelete(Mockito.any(List.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBatchSave_CallsCorrectDynamoDBMapperMethod()
	{
			List<User> users = new ArrayList<User>();
			dynamoDBTemplate.batchSave(users);
			Mockito.verify(dynamoDBMapper).batchSave(Mockito.any(List.class));
	}
	
	@Test
	public void testGetOverriddenTableName_WhenConfigIsNull()
	{
		String overriddenTableName = dynamoDBTemplate.getOverriddenTableName(Object.class, "someTableName");
		Assert.assertEquals("someTableName", overriddenTableName);
	}
	
    @Test
    public void testGetOverriddenTableName()
    {
        String overriddenTableName = dynamoDBTemplate.getOverriddenTableName(Object.class, "someTableName");
        Assert.assertEquals("someTableName", overriddenTableName);
    }

    @Test
    public void testGetOverriddenTableName_WithTableNameResolver()
    {
        DynamoDBMapperConfig dynamoDBMapperConfig = Mockito.mock(DynamoDBMapperConfig.class);
        TableNameResolver tableNameResolver = Mockito.mock(TableNameResolver.class);
        Mockito.when(tableNameResolver.getTableName(Object.class, dynamoDBMapperConfig)).thenReturn(
            "someOtherTableName");
        Mockito.when(dynamoDBMapperConfig.getTableNameResolver()).thenReturn(tableNameResolver);
        dynamoDBTemplate.setDynamoDBMapperConfig(dynamoDBMapperConfig);
        String overriddenTableName = dynamoDBTemplate.getOverriddenTableName(Object.class, "someTableName");
        Assert.assertEquals("someOtherTableName", overriddenTableName);
    }

	@Test
	public void testLoadByHashKey_WhenDynamoDBMapperReturnsNull()
	{
		User user = dynamoDBTemplate.load(User.class, "someHashKey");	
		Assert.assertNull(user);
	}
	
	@Test
	public void testLoadByHashKeyAndRangeKey_WhenDynamoDBMapperReturnsNull()
	{
		Playlist playlist = dynamoDBTemplate.load(Playlist.class, "someHashKey","someRangeKey");	
		Assert.assertNull(playlist);
	}

}
