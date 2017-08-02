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
package org.socialsignin.spring.data.dynamodb.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.mockito.Mockito.mock;
import static org.socialsignin.spring.data.dynamodb.utils.SortHandler.ensureNoSort;
import static org.socialsignin.spring.data.dynamodb.utils.SortHandler.throwUnsupportedSortOperationException;

public class SortHandlerTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testThrowUnsupportedSortException() {
        expectedException.expect(UnsupportedOperationException.class);
        
        throwUnsupportedSortOperationException();
    }

    @Test
    public void testEnsureNoSortUnsorted() {
        ensureNoSort((Sort)null);
    }

    @Test
    public void testEnsureNoSortSorted() {
        expectedException.expect(UnsupportedOperationException.class);

        ensureNoSort(new Sort("property"));
    }

    @Test
    public void testEnsureNoSortUnpaged() {
        ensureNoSort(mock(Pageable.class));
    }

    @Test
    public void TestEnsureNoSortPagedUnsorted() {
        ensureNoSort(new PageRequest(0, 1, null));
    }

    @Test
    public void TestEnsureNoSortPagedSorted() {
        expectedException.expect(UnsupportedOperationException.class);

        ensureNoSort(new PageRequest(0, 1, new Sort("property")));
    }
}
