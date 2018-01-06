package org.socialsignin.spring.data.dynamodb.query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountByHashKeyQueryTest {
    private static final Class<User> DOMAIN_CLASS = User.class;
    private static final Random r = new Random();
    @Mock
    private DynamoDBOperations dynamoDBOperations;
    @Mock
    private User sampleEntity;
    private Object hashKey;
    private CountByHashKeyQuery underTest;

    @Before
    public void setUp() {
        hashKey = r.nextLong();
        underTest = new CountByHashKeyQuery(dynamoDBOperations, DOMAIN_CLASS, hashKey);
    }

    @Test
    public void testGetSingleResultExists() {
        when(dynamoDBOperations.load(DOMAIN_CLASS, hashKey)).thenReturn(sampleEntity);
        Long actual = underTest.getSingleResult();

        assertEquals(Long.valueOf(1), actual);
    }

    @Test
    public void testGetSingleResultDoesntExist() {
        when(dynamoDBOperations.load(DOMAIN_CLASS, hashKey)).thenReturn(null);
        Long actual = underTest.getSingleResult();

        assertEquals(Long.valueOf(0), actual);
    }
}
