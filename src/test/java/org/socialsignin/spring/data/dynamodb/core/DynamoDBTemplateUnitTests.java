package org.socialsignin.spring.data.dynamodb.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBTemplateUnitTests {
	
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
	
	@Test
	public void testBatchDelete_CallsCorrectDynamoDBMapperMethod()
	{
			Iterable<User> users = new ArrayList<User>();
			dynamoDBTemplate.batchDelete(users);
			Mockito.verify(dynamoDBMapper).batchDelete(Mockito.any(List.class));
	}
	
	@Test
	public void testBatchSave_CallsCorrectDynamoDBMapperMethod()
	{
			Iterable<User> users = new ArrayList<User>();
			dynamoDBTemplate.batchSave(users);
			Mockito.verify(dynamoDBMapper).batchSave(Mockito.any(List.class));
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
