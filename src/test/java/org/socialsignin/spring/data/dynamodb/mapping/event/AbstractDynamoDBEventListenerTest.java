/**
 * Copyright © 2013 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.mapping.event;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
        when(sampleQueryList.stream()).thenReturn(queryList.stream());
        when(sampleScanList.stream()).thenReturn(queryList.stream());
    }

    @Test
    public void testAfterDelete() {
        underTest.onApplicationEvent(new AfterDeleteEvent<User>(sampleEntity));

        verify(underTest).onAfterDelete(sampleEntity);
        verify(underTest, never()).onAfterLoad(any());
        verify(underTest, never()).onAfterQuery(any());
        verify(underTest, never()).onAfterSave(any());
        verify(underTest, never()).onAfterScan(any());
        verify(underTest, never()).onBeforeDelete(any());
        verify(underTest, never()).onBeforeSave(any());
    }

    @Test
    public void testAfterLoad() {
        underTest.onApplicationEvent(new AfterLoadEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any());
        verify(underTest).onAfterLoad(sampleEntity);
        verify(underTest, never()).onAfterQuery(any());
        verify(underTest, never()).onAfterSave(any());
        verify(underTest, never()).onAfterScan(any());
        verify(underTest, never()).onBeforeDelete(any());
        verify(underTest, never()).onBeforeSave(any());
    }

    @Test
    public void testAfterQuery() {
        underTest.onApplicationEvent(new AfterQueryEvent<User>(sampleQueryList));

        verify(underTest, never()).onAfterDelete(any());
        verify(underTest, never()).onAfterLoad(any());
        verify(underTest).onAfterQuery(sampleEntity);
        verify(underTest, never()).onAfterSave(any());
        verify(underTest, never()).onAfterScan(any());
        verify(underTest, never()).onBeforeDelete(any());
        verify(underTest, never()).onBeforeSave(any());
    }

    @Test
    public void testAfterSave() {
        underTest.onApplicationEvent(new AfterSaveEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any());
        verify(underTest, never()).onAfterLoad(any());
        verify(underTest, never()).onAfterQuery(any());
        verify(underTest).onAfterSave(sampleEntity);
        verify(underTest, never()).onAfterScan(any());
        verify(underTest, never()).onBeforeDelete(any());
        verify(underTest, never()).onBeforeSave(any());
    }

    @Test
    public void testAfterScan() {
        underTest.onApplicationEvent(new AfterScanEvent<>(sampleScanList));

        verify(underTest, never()).onAfterDelete(any());
        verify(underTest, never()).onAfterLoad(any());
        verify(underTest, never()).onAfterQuery(any());
        verify(underTest, never()).onAfterSave(any());
        verify(underTest).onAfterScan(sampleEntity);
        verify(underTest, never()).onBeforeDelete(any());
        verify(underTest, never()).onBeforeSave(any());
    }

    @Test
    public void testBeforeDelete() {
        underTest.onApplicationEvent(new BeforeDeleteEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any());
        verify(underTest, never()).onAfterLoad(any());
        verify(underTest, never()).onAfterQuery(any());
        verify(underTest, never()).onAfterSave(any());
        verify(underTest, never()).onAfterScan(any());
        verify(underTest).onBeforeDelete(sampleEntity);
        verify(underTest, never()).onBeforeSave(any());
    }

    @Test
    public void testBeforeSave() {
        underTest.onApplicationEvent(new BeforeSaveEvent<>(sampleEntity));

        verify(underTest, never()).onAfterDelete(any());
        verify(underTest, never()).onAfterLoad(any());
        verify(underTest, never()).onAfterQuery(any());
        verify(underTest, never()).onAfterSave(any());
        verify(underTest, never()).onAfterScan(any());
        verify(underTest, never()).onBeforeDelete(any());
        verify(underTest).onBeforeSave(sampleEntity);
    }




}
