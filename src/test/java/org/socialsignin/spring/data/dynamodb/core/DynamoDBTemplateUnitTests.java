package org.socialsignin.spring.data.dynamodb.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
