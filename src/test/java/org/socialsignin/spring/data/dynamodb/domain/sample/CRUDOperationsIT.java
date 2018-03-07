/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/spring-data-dynamodb/spring-data-dynamodb)
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
package org.socialsignin.spring.data.dynamodb.domain.sample;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.socialsignin.spring.data.dynamodb.utils.DynamoDBLocalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DynamoDBLocalResource.class, CRUDOperationsIT.TestAppConfig.class})
public class CRUDOperationsIT {

    private static final Random r = new Random();

    @Configuration
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
    public static class TestAppConfig {
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonDynamoDB ddb;

    @Before
    public void setUp() {
        DynamoDBLocalResource.createTable(ddb, User.class);
    }

    @Test
    public void testDelete() throws InterruptedException {
        // Prepare
        User u1 = new User();
        String name1 = "name1" + r.nextLong();
        u1.setName(name1);
        u1.setId("u1");

        User u2 = new User();
        String name2 = "name1" + r.nextLong();
        u2.setId("u2");
        u2.setName(name2);

        User u3 = new User();
        String name3 = "name1" + r.nextLong();
        u3.setId("u3");
        u3.setName(name3);


        userRepository.save(u1);
        userRepository.save(u2);
        userRepository.save(u3);

        List<User> actualList = new ArrayList<>();
        userRepository.findAll().forEach(actualList::add);
        assertEquals("Unexpected List: " + actualList, 3, actualList.size());
        actualList.clear();


        userRepository.findByNameIn(Arrays.asList(name1, name2)).forEach(actualList::add);
        assertEquals("Unexpected List: " + actualList, 2, actualList.size());
        actualList.clear();

        // Delete specific
        userRepository.deleteById("u2");
        userRepository.findAll().forEach(actualList::add);
        assertEquals("u1", actualList.get(0).getId());
        assertEquals("u3", actualList.get(1).getId());

        //Delete conditional
        userRepository.deleteByIdAndName("u1", name1);
        Optional<User> actualUser = userRepository.findById("u1");
        assertFalse("User should have been deleted!", actualUser.isPresent());
    }

}
