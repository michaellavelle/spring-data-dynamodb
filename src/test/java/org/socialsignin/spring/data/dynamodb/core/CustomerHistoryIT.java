package org.socialsignin.spring.data.dynamodb.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.domain.sample.CustomerHistory;
import org.socialsignin.spring.data.dynamodb.domain.sample.CustomerHistoryRepository;
import org.socialsignin.spring.data.dynamodb.domain.sample.FeedUserRepository;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={CustomerHistoryIT.TestAppConfig.class, ConfigurationTI.class})
public class CustomerHistoryIT {

    @Configuration
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
    public static class TestAppConfig {
    }
    
    @Autowired
    CustomerHistoryRepository customerHistoryRepository;

    @Test
    public void saveAndGSITest() {
        
        CustomerHistory expected = new CustomerHistory();
        expected.setId("customerId");
        expected.setCreateDt("createDTt");
        expected.setTag("2342");
        
        customerHistoryRepository.save(expected);
        
        CustomerHistory actual = customerHistoryRepository.findByTag(expected.getTag());
        
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCreateDt(), actual.getCreateDt());
        assertEquals(expected.getTag(), actual.getTag());
    }
}
