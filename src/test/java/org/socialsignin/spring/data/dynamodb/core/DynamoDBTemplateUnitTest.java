package org.socialsignin.spring.data.dynamodb.core;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameResolver;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBTemplateUnitTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private DynamoDBMapper dynamoDBMapper;
	@Mock
	private AmazonDynamoDB dynamoDB;
	@Mock
	private DynamoDBMapperConfig dynamoDBMapperConfig;
	@Mock
	private ApplicationContext applicationContext;
	
	private DynamoDBTemplate dynamoDBTemplate;

	@Before
	public void setUp() {
		this.dynamoDBTemplate = new DynamoDBTemplate(dynamoDB, dynamoDBMapperConfig, dynamoDBMapper);
		this.dynamoDBTemplate.setApplicationContext(applicationContext);
	}
	
	@Test
	public void testConstructorMandatory() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("must not be null!");
		new DynamoDBTemplate(null);
	}

	@Test
	public void testConstructorOptionalAllNull() {
		dynamoDBTemplate = new DynamoDBTemplate(dynamoDB, null, null);
		
		// check that the defaults are properly initialized - #108
		String userTableName = dynamoDBTemplate.getOverriddenTableName(User.class, "UserTable");
		assertEquals("user", userTableName);
	}

	@Test
	public void testConstructorOptionalPreconfiguredDynamoDBMapper() {
		// Introduced constructor via #91 should not fail its assert
		DynamoDBTemplate usePreconfiguredDynamoDBMapper = new DynamoDBTemplate(dynamoDB, dynamoDBMapper);

		assertTrue("The constructor should not fail with an assert error", true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBatchDelete_CallsCorrectDynamoDBMapperMethod()
	{
			List<User> users = new ArrayList<>();
			dynamoDBTemplate.batchDelete(users);
			Mockito.verify(dynamoDBMapper).batchDelete(Mockito.any(List.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBatchSave_CallsCorrectDynamoDBMapperMethod()
	{
			List<User> users = new ArrayList<>();
			dynamoDBTemplate.batchSave(users);
			Mockito.verify(dynamoDBMapper).batchSave(Mockito.any(List.class));
	}
	
	@Test
	public void testGetOverriddenTableName_WhenConfigIsNull()
	{
	    String overriddenTableName = dynamoDBTemplate.getOverriddenTableName(User.class, "someTableName");
		Assert.assertEquals("someTableName", overriddenTableName);
	}
	
    @Test
    public void testGetOverriddenTableName()
    {
        String overriddenTableName = dynamoDBTemplate.getOverriddenTableName(User.class, "someTableName");
        Assert.assertEquals("someTableName", overriddenTableName);
    }

    @Test
    public void testGetOverriddenTableName_WithTableNameResolver()
    {
        TableNameResolver tableNameResolver = Mockito.mock(TableNameResolver.class);
        Mockito.when(tableNameResolver.getTableName(Object.class, dynamoDBMapperConfig)).thenReturn(
            "someOtherTableName");
        Mockito.when(dynamoDBMapperConfig.getTableNameResolver()).thenReturn(tableNameResolver);
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
