package org.socialsignin.spring.data.dynamodb.mapping.event;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class LoggingEventListenerTest {

    private final TestLogger logger = TestLoggerFactory.getTestLogger(LoggingEventListener.class);
    private final User sampleEntity = new User();
    @Mock
    private PaginatedQueryList<User> sampleQueryList;
    @Mock
    private PaginatedScanList<User> sampleScanList;

    private LoggingEventListener underTest;

    @Before
    public void setUp() {
        underTest = new LoggingEventListener();

        logger.setEnabledLevels(Level.TRACE);

        List<User> queryList = new ArrayList<>();
        queryList.add(sampleEntity);
        when(sampleQueryList.stream()).thenReturn(queryList.stream());
        when(sampleScanList.stream()).thenReturn(queryList.stream());
    }

    @After
    public void clearLoggers() {
        TestLoggerFactory.clear();
    }

    @Test
    public void testAfterDelete() {
        underTest.onApplicationEvent(new AfterDeleteEvent<>(sampleEntity));

        assertThat(logger.getLoggingEvents(), is(asList(trace("onAfterDelete: {}", sampleEntity))));
    }

    @Test
    public void testAfterLoad() {
        underTest.onApplicationEvent(new AfterLoadEvent<>(sampleEntity));

        assertThat(logger.getLoggingEvents(), is(asList(trace("onAfterLoad: {}", sampleEntity))));
    }

    @Test
    public void testAfterQuery() {
        underTest.onApplicationEvent(new AfterQueryEvent<>(sampleQueryList));

        assertThat(logger.getLoggingEvents(), is(asList(trace("onAfterQuery: {}", sampleEntity))));
    }

    @Test
    public void testAfterSave() {
        underTest.onApplicationEvent(new AfterSaveEvent<>(sampleEntity));

        assertThat(logger.getLoggingEvents(), is(asList(trace("onAfterSave: {}", sampleEntity))));
    }

    @Test
    public void testAfterScan() {
        underTest.onApplicationEvent(new AfterScanEvent<>(sampleScanList));

        assertThat(logger.getLoggingEvents(), is(asList(trace("onAfterScan: {}", sampleEntity))));
    }

    @Test
    public void testBeforeDelete() {
        underTest.onApplicationEvent(new BeforeDeleteEvent<>(sampleEntity));

        assertThat(logger.getLoggingEvents(), is(asList(trace("onBeforeDelete: {}", sampleEntity))));
    }

    @Test
    public void testBeforeSave() {
        underTest.onApplicationEvent(new BeforeSaveEvent<>(sampleEntity));

        assertThat(logger.getLoggingEvents(), is(asList(trace("onBeforeSave: {}", sampleEntity))));
    }

}
