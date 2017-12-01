package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.socialsignin.spring.data.dynamodb.core.ConfigurationTI;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * Shows the usage of Hash+Range key combinations with global secondary indexes.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConfigurationTI.class, GlobalSecondaryIndexWithRangeKeyIT.TestAppConfig.class})
public class GlobalSecondaryIndexWithRangeKeyIT {

    @Configuration
    @EnableDynamoDBRepositories(basePackages = "org.socialsignin.spring.data.dynamodb.domain.sample")
    public static class TestAppConfig {
    }

    @Autowired
    private InstallationRepository installationRepository;

    @Test
    public void testFindBySystemIdOrderByUpdatedAtDesc() {
        installationRepository.save(new Installation("systemId", createDate(10, 5, 1995)));
        installationRepository.save(new Installation("systemId", createDate(20, 10, 2001)));
        installationRepository.save(new Installation("systemId", createDate(28, 10, 2016)));

        final List<Installation> actual = installationRepository.findBySystemIdOrderByUpdatedAtDesc("systemId");
        assertNotNull(actual);
        assertFalse(actual.isEmpty());

        Date previousDate = null;
        for (final Installation installation : actual) {
            assertEquals(installation.getSystemId(), "systemId");
            if (previousDate != null && installation.getUpdatedAt().compareTo(previousDate) != -1) {
                fail("Results were not returned in descending order of updated date!");
            } else {
                previousDate = installation.getUpdatedAt();
            }
        }
    }

    private Date createDate(final int dayOfMonth, final int month, final int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}

