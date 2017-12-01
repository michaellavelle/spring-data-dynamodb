package org.socialsignin.spring.data.dynamodb.core;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Clue {@link Configuration} class for all integration tests.
 * It exposes the {@link AmazonDynamoDB} pre-configured to use the
 * launched local DynamoDB by Maven's integration-test.
 */
@Configuration
public class ConfigurationTI {
    private static final String DYNAMODB_PORT_PROPERTY = "dynamodb.port";
    private static final String PORT = System.getProperty(DYNAMODB_PORT_PROPERTY);

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        Assert.notNull(PORT, "System property '" + DYNAMODB_PORT_PROPERTY + " not set!");

        AmazonDynamoDB dynamoDB = new AmazonDynamoDBClient(new BasicAWSCredentials("AWS-Key", ""));
        dynamoDB.setEndpoint(String.format("http://localhost:%s", PORT));

        return dynamoDB;
    }
}
