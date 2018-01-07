package org.socialsignin.spring.data.dynamodb.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBHashAndRangeKeyTest {

    @Mock
    private Object hash;
    @Mock
    private Object range;

    @Test
    public void testConstructor() {
        DynamoDBHashAndRangeKey underTest = new DynamoDBHashAndRangeKey(hash, range);

        assertEquals(hash, underTest.getHashKey());
        assertEquals(range, underTest.getRangeKey());
    }

    @Test
    public void testDefaultConstructor() {
        DynamoDBHashAndRangeKey underTest = new DynamoDBHashAndRangeKey();

        assertNull(underTest.getHashKey());
        assertNull(underTest.getRangeKey());
    }

    @Test
    public void testGetterSetter() {
        DynamoDBHashAndRangeKey underTest = new DynamoDBHashAndRangeKey();

        underTest.setHashKey(hash);
        underTest.setRangeKey(range);

        assertEquals(hash, underTest.getHashKey());
        assertEquals(range, underTest.getRangeKey());
    }
}
