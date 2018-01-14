package org.socialsignin.spring.data.dynamodb.mapping.event;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDynamoDBEventListenerTest {

    private User sampleEntity = new User();
    @Mock
    private PaginatedQueryList<User> sampleQueryList;
    @Mock
    private PaginatedScanList<User> sampleScanList;

    private AbstractDynamoDBEventListener<User> underTest;

    @Before
    public void setUp() {
        underTest = Mockito.spy(new AbstractDynamoDBEventListener<User>() {
        });

        List<User> queryList = new ArrayList<>();
        queryList.add(sampleEntity);
        when(sampleQueryList.iterator()).thenReturn(queryList.iterator());
        when(sampleScanList.iterator()).thenReturn(queryList.iterator());
    }

    @Test
    public void testAfterDelete() {
        underTest.onApplicationEvent(new AfterDeleteEvent<>(sampleEntity));

        verify(underTest).onAfterDelete(sampleEntity);
        verify(underTest, never()).onAfterLoad(any(User.class));
        verify(underTest, never()).onAfterQuery(any(User.class));
        verify(underTest, never()).onAfterSave(any(User.class));
        verify(underTest, never()).onAfterScan(any(User.class));
        verify(underTest, never()).onBeforeDelete(any(User.class));
        verify(underTest, never()).onBeforeSave(any(User.class));
    }

    @Test
    public void testAfterLoad() {
        underTest.onApplicationEvent(new AfterLoadEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any(User.class));
        verify(underTest).onAfterLoad(sampleEntity);
        verify(underTest, never()).onAfterQuery(any(User.class));
        verify(underTest, never()).onAfterSave(any(User.class));
        verify(underTest, never()).onAfterScan(any(User.class));
        verify(underTest, never()).onBeforeDelete(any(User.class));
        verify(underTest, never()).onBeforeSave(any(User.class));
    }

    @Test
    public void testAfterQuery() {
        underTest.onApplicationEvent(new AfterQueryEvent<>(sampleQueryList));

        verify(underTest, never()).onAfterDelete(any(User.class));
        verify(underTest, never()).onAfterLoad(any(User.class));
        verify(underTest).onAfterQuery(sampleEntity);
        verify(underTest, never()).onAfterSave(any(User.class));
        verify(underTest, never()).onAfterScan(any(User.class));
        verify(underTest, never()).onBeforeDelete(any(User.class));
        verify(underTest, never()).onBeforeSave(any(User.class));
    }

    @Test
    public void testAfterSave() {
        underTest.onApplicationEvent(new AfterSaveEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any(User.class));
        verify(underTest, never()).onAfterLoad(any(User.class));
        verify(underTest, never()).onAfterQuery(any(User.class));
        verify(underTest).onAfterSave(sampleEntity);
        verify(underTest, never()).onAfterScan(any(User.class));
        verify(underTest, never()).onBeforeDelete(any(User.class));
        verify(underTest, never()).onBeforeSave(any(User.class));
    }

    @Test
    public void testAfterScan() {
        underTest.onApplicationEvent(new AfterScanEvent<>(sampleScanList));

        verify(underTest, never()).onAfterDelete(any(User.class));
        verify(underTest, never()).onAfterLoad(any(User.class));
        verify(underTest, never()).onAfterQuery(any(User.class));
        verify(underTest, never()).onAfterSave(any(User.class));
        verify(underTest).onAfterScan(sampleEntity);
        verify(underTest, never()).onBeforeDelete(any(User.class));
        verify(underTest, never()).onBeforeSave(any(User.class));
    }

    @Test
    public void testBeforeDelete() {
        underTest.onApplicationEvent(new BeforeDeleteEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any(User.class));
        verify(underTest, never()).onAfterLoad(any(User.class));
        verify(underTest, never()).onAfterQuery(any(User.class));
        verify(underTest, never()).onAfterSave(any(User.class));
        verify(underTest, never()).onAfterScan(any(User.class));
        verify(underTest).onBeforeDelete(sampleEntity);
        verify(underTest, never()).onBeforeSave(any(User.class));
    }

    @Test
    public void testBeforeSave() {
        underTest.onApplicationEvent(new BeforeSaveEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any(User.class));
        verify(underTest, never()).onAfterLoad(any(User.class));
        verify(underTest, never()).onAfterQuery(any(User.class));
        verify(underTest, never()).onAfterSave(any(User.class));
        verify(underTest, never()).onAfterScan(any(User.class));
        verify(underTest, never()).onBeforeDelete(any(User.class));
        verify(underTest).onBeforeSave(sampleEntity);
    }




}
