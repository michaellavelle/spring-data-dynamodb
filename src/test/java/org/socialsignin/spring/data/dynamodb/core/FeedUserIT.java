package org.socialsignin.spring.data.dynamodb.core;

import org.junit.Test;
import org.junit.runner.RunWith;
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
@ContextConfiguration(classes={FeedUserIT.TestAppConfig.class, ConfigurationTI.class})
public class FeedUserIT {

    @Configuration
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
    public static class TestAppConfig {
    }

    @Autowired
    FeedUserRepository feedUserRepository;

    @Test
    public void feed_test(){
        PageRequest pageRequest = new PageRequest(1, 10, new Sort(Direction.DESC, "usrNo"));
        feedUserRepository.findByUsrNo(2, pageRequest); //runnable
        feedUserRepository.findByUsrNoAndFeedOpenYn(2, true, pageRequest); //not runnable
    }
}
