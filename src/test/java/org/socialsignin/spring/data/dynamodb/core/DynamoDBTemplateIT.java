package org.socialsignin.spring.data.dynamodb.core;

import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Integration test that interacts with DynamoDB Local instance.
 */
@Ignore
public class DynamoDBTemplateIT {

    private static final String PORT = System.getProperty("dynamodb.port");

    private DynamoDBTemplate dynamoDBTemplate;

    @Before
    public void setUp() {
        AmazonDynamoDB dynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials("AWS-Key", ""));
        dynamoDB.setEndpoint(String.format("http://localhost:%s", DynamoDBTemplateIT.PORT));

        this.dynamoDBTemplate = new DynamoDBTemplate(dynamoDB);
    }

    @Test
    public void testUser_CRUD() {

        // Given a entity to save.
        User user = new User();
        user.setName("John Doe");
        user.setNumberOfPlaylists(10);
        user.setId(UUID.randomUUID().toString());

        // Save it to DB.
        dynamoDBTemplate.save(user);

        // Retrieve it from DB.
        User retrievedUser = dynamoDBTemplate.load(User.class, user.getId());

        // Verify the details on the entity.
        assert retrievedUser.getName().equals(user.getName());
        assert retrievedUser.getId().equals(user.getId());
        assert retrievedUser.getNumberOfPlaylists() == user.getNumberOfPlaylists();

        // Update the entity and save.
        retrievedUser.setNumberOfPlaylists(20);
        dynamoDBTemplate.save(retrievedUser);

        retrievedUser = dynamoDBTemplate.load(User.class, user.getId());

        assert retrievedUser.getNumberOfPlaylists() == 20;

        // Delete.
        dynamoDBTemplate.delete(retrievedUser);

        // Get again.
        assert dynamoDBTemplate.load(User.class, user.getId()) == null;

    }

}
