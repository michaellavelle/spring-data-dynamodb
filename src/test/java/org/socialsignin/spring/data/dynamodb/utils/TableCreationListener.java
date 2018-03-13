/**
 * Copyright © 2018 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

public class TableCreationListener extends AbstractTestExecutionListener {

    public static <T> CreateTableResult createTable(AmazonDynamoDB ddb, Class<T> domainType) {
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        ProvisionedThroughput pt = new ProvisionedThroughput(10L, 10L);

        CreateTableRequest ctr = mapper.generateCreateTableRequest(domainType);
        ctr.setProvisionedThroughput(pt);
        if (ctr.getGlobalSecondaryIndexes() != null) {
            ctr.getGlobalSecondaryIndexes().forEach(gsi -> {
                gsi.setProjection(new Projection().withProjectionType(ProjectionType.ALL));
                gsi.setProvisionedThroughput(pt);
            });
        }

        CreateTableResult ctResponse = ddb.createTable(ctr);

        do {
            try {
                Thread.sleep(1 * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException("Couldn't wait detect table " + ctr.getTableName());
            }
        }
        while (!ddb.describeTable(ctr.getTableName()).getTable().getTableStatus().equals("ACTIVE"));

        return ctResponse;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    public @interface DynamoDBCreateTable {
        Class<?>[] entityClasses();
    }

    protected Class<?>[] getEntityClasses() {
        return new Class<?>[0];
    }

    private void createExpliclitEntities(TestContext testContext) {
        BeanFactory bf = testContext.getApplicationContext().getAutowireCapableBeanFactory();

        AmazonDynamoDB ddb = bf.getBean(AmazonDynamoDB.class);

        Arrays.stream(getEntityClasses()).forEach(clazz -> createTable(ddb, clazz));
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        createExpliclitEntities(testContext);
        createAnnotationEntities(testContext);
    }

    private void createAnnotationEntities(TestContext testContext) {

        Class<?> testClass = testContext.getTestClass();
        if (testClass.isAnnotationPresent(DynamoDBCreateTable.class)) {

            DynamoDBCreateTable createTableAnnotation = testClass.getAnnotation(DynamoDBCreateTable.class);

            AmazonDynamoDB ddb = getAmazonDynamoDB(testContext);

            for (Class<?> entityClass : createTableAnnotation.entityClasses()) {
                createTable(ddb, entityClass);
            }
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        getAmazonDynamoDB(testContext).shutdown();
    }

    private BeanFactory getBeanFactory(TestContext testContext) {
        return testContext.getApplicationContext().getAutowireCapableBeanFactory();
    }

    private AmazonDynamoDB getAmazonDynamoDB(TestContext testContext) {
        return getBeanFactory(testContext).getBean(AmazonDynamoDB.class);
    }
}
