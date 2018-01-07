package org.socialsignin.spring.data.dynamodb.repository.cdi;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBRepositoryBeanTest {

    interface SampleRepository {
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private BeanManager beanManager;
    @Mock
    private Bean<AmazonDynamoDB> amazonDynamoDBBean;
    @Mock
    private javax.enterprise.inject.spi.Bean<DynamoDBMapperConfig> dynamoDBMapperConfigBean;
    @Mock
    private Bean<DynamoDBOperations> dynamoDBOperationsBean;

    private Set<Annotation> qualifiers = Collections.emptySet();
    private Class<?> repositoryType = SampleRepository.class;

    @Test
    public void testNullOperationsOk() {
        DynamoDBRepositoryBean underTest = new DynamoDBRepositoryBean(beanManager, amazonDynamoDBBean,
                dynamoDBMapperConfigBean, null, qualifiers, repositoryType);

        assertNotNull(underTest);
    }

    @Test
    public void testNullOperationFail() {
        expectedException.expectMessage("amazonDynamoDBBean must not be null!");

        DynamoDBRepositoryBean underTest = new DynamoDBRepositoryBean(beanManager, null,
                dynamoDBMapperConfigBean, null, qualifiers, repositoryType);
    }

    @Test
    public void testSetOperationOk1() {
        DynamoDBRepositoryBean underTest = new DynamoDBRepositoryBean(beanManager, null,
                null, dynamoDBOperationsBean, qualifiers, repositoryType);

        assertNotNull(underTest);
    }

    @Test
    public void testSetOperationFail1() {
        expectedException.expectMessage("Cannot specify both dynamoDBMapperConfigBean bean and dynamoDBOperationsBean in repository configuration");

        DynamoDBRepositoryBean underTest = new DynamoDBRepositoryBean(beanManager, null,
                dynamoDBMapperConfigBean, dynamoDBOperationsBean, qualifiers, repositoryType);
    }

    @Test
    public void testSetOperationFail2() {
        expectedException.expectMessage("Cannot specify both amazonDynamoDB bean and dynamoDBOperationsBean in repository configuration");

        DynamoDBRepositoryBean underTest = new DynamoDBRepositoryBean(beanManager, amazonDynamoDBBean,
                null, dynamoDBOperationsBean, qualifiers, repositoryType);
    }
}
