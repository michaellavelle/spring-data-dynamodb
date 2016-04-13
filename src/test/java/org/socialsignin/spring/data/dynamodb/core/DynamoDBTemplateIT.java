package org.socialsignin.spring.data.dynamodb.core;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

/**
 * Integration test that interacts with DynamoDB Local instance.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ConfigurationTI.class})
public class DynamoDBTemplateIT {

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;
    private DynamoDBTemplate dynamoDBTemplate;

    @Before
    public void setUp() {
        this.dynamoDBTemplate = new DynamoDBTemplate(amazonDynamoDB);
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
