package org.socialsignin.spring.data.dynamodb.query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AbstractQueryTest {

    private static class QueryTest<T> extends AbstractQuery<T> {
        public QueryTest(DynamoDBOperations dynamoDBOperations, Class<T> clazz) {
            super(dynamoDBOperations, clazz);
        }

        @Override
        public List<T> getResultList() {
            return null;
        }

        @Override
        public T getSingleResult() {
            return null;
        }
    }

    @Mock
    private DynamoDBOperations dynamoDBOperations;
    private AbstractQuery<User> underTest;

    @Before
    public void setUp() {
        underTest = new QueryTest<>(dynamoDBOperations, User.class);
    }

    @Test
    public void testSetter() {
        assertFalse(underTest.isScanCountEnabled());
        assertFalse(underTest.isScanEnabled());

        underTest.setScanCountEnabled(true);
        underTest.setScanEnabled(true);

        assertTrue(underTest.isScanCountEnabled());
        assertTrue(underTest.isScanEnabled());
    }
    
}
