[![codecov.io](https://codecov.io/github/derjust/spring-data-dynamodb/coverage.svg?branch=master)](https://codecov.io/github/derjust/spring-data-dynamodb?branch=master) [![Build Status](https://travis-ci.org/derjust/spring-data-dynamodb.svg?branch=master)](https://travis-ci.org/derjust/spring-data-dynamodb) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.derjust/spring-data-dynamodb/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.derjust/spring-data-dynamodb)
[![Donation badge](https://img.shields.io/badge/Donate-%F0%9F%92%B8-DAA520.svg)](DONATION.md)

# ⚠ Planing to [transfer](https://help.github.com/articles/transferring-a-repository-owned-by-your-personal-account/) this repository to the [spring-data-dynamodb](https://github.com/spring-data-dynamodb) organization by the end of the month! ⚠ #
Everything should stay the same for links/browsing - just Git `remote`s must be updated


# Spring  Data DynamoDB #

<img align="left" src="https://derjust.github.io/spring-data-dynamodb/banner/spring-data-dynamodb.png">

The primary goal of the [Spring® Data](http://www.springsource.org/spring-data) project is to make it easier to build Spring-powered applications that use data access technologies.

This module deals with enhanced support for a data access layer built on [AWS DynamoDB](https://aws.amazon.com/dynamodb/).

Technical infos can be found on the [project page](https://derjust.github.io/spring-data-dynamodb/).

## Supported Features ##

* Implementation of CRUD methods for DynamoDB Entities
* Dynamic query generation from query method names  (Only a limited number of keywords and comparison operators currently supported)
* Possibility to integrate [custom repository code](https://github.com/derjust/spring-data-dynamodb/wiki/Custom-repository-implementations)
* Easy Spring annotation based integration

## Demo application ##

For a demo of spring-data-dynamodb, using spring-data-rest to showcase DynamoDB repositories exposed with REST,
please see [spring-data-dynamodb-demo](https://github.com/michaellavelle/spring-data-dynamodb-demo).

## Version & Spring Framework compatibility ##

The major and minor number of this library refers to the compatible Spring framework version. The build number is used as specified by SEMVER.

API changes will follow SEMVER and loosly the Spring Framework releases.

| `spring-data-dynamodb` version  | Spring Boot compatibility      |Spring Framework compatibility  | Spring Data compatibility |
| ------------------------------- | ------------------------------ | ------------------------------ | ------------------------- |
| 1.0.x                           |                                | >= 3.1 && < 4.2                |                           |
| 4.2.x                           | >= 1.3.0 && < 1.4.0            | >= 4.2 && < 4.3                | Gosling-SR1               |
| 4.3.x                           | >= 1.4.0 < 2.0                 | >= 4.3 && < 5.0                | Gosling-SR1               |
| 4.4.x                           | >= 1.4.0 < 2.0                 | >= 4.3 && < 5.0                | Hopper-SR2                |
| 4.5.x                           | >= 1.4.0 < 2.0                 | >= 4.3 && < 5.0                | Ingalls                   |
| 5.0.x                           | >= 2.0                         | >= 5.0                         | Kay-SR1                   |

`spring-data-dynamodb` depends directly on `spring-data` as also `spring-context`, `spring-data` and `spring-tx`.

`compile` and `runtime` dependencies are kept to a minimum to allow easy integartion, for example into 
Spring-Boot projects.

## Quick Start ##

Download the JAR though [Maven](http://mvnrepository.com/artifact/com.github.derjust/spring-data-dynamodb):

```xml
<dependency>
  <groupId>com.github.derjust</groupId>
  <artifactId>spring-data-dynamodb</artifactId>
  <version>4.5.5</version>
</dependency>
```

or via Gradle

```yml
repositories {
  mavenCentral()
}

dependencies {
  compile group: 'com.github.derjust',
  name: 'spring-data-dynamodb',
  version: '4.5.5'
}
```

Setup DynamoDB configuration as well as enabling Spring Data DynamoDB repository support.

```java
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.acme.repositories")
public class DynamoDBConfig {

	@Value("${amazon.dynamodb.endpoint}")
	private String amazonDynamoDBEndpoint;

	@Value("${amazon.aws.accesskey}")
	private String amazonAWSAccessKey;

	@Value("${amazon.aws.secretkey}")
	private String amazonAWSSecretKey;

	@Bean
	public AmazonDynamoDB amazonDynamoDB(AWSCredentials amazonAWSCredentials) {
		AmazonDynamoDB amazonDynamoDB = new AmazonDynamoDBClient(amazonAWSCredentials);

		if (StringUtils.isNotEmpty(amazonDynamoDBEndpoint)) {
			amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
		}
		return amazonDynamoDB;
	}

	@Bean
	public AWSCredentials amazonAWSCredentials() {
	    // Or use an AWSCredentialsProvider/AWSCredentialsProviderChain
		return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
	}

}
```

or in XML...

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dynamodb="http://docs.socialsignin.org/schema/data/dynamodb"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://docs.socialsignin.org/schema/data/dynamodb
                           http://docs.socialsignin.org/schema/data/dynamodb/spring-dynamodb.xsd">

  <bean id="amazonDynamoDB" class="com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient">
    <constructor-arg ref="amazonAWSCredentials" />
    <property name="endpoint" value="${amazon.dynamodb.endpoint}" />
  </bean>
  
  <bean id="amazonAWSCredentials" class="com.amazonaws.auth.BasicAWSCredentials">
    <constructor-arg value="${amazon.aws.accesskey}" />
    <constructor-arg value="${amazon.aws.secretkey}" />
  </bean>
  
  <dynamodb:repositories base-package="com.acme.repositories" amazon-dynamodb-ref="amazonDynamoDB" />
  
</beans>

```

Create a DynamoDB hash-key only table in AWS console, with table name `User` and with hash key attribute name `id`.

Create a DynamoDB entity for this table:

```java
@DynamoDBTable(tableName = "User")
public class User {

  private String id;
  private String firstName;
  private String lastName;

  public User() {
  }
  
  public User(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @DynamoDBHashKey
  @DynamoDBAutoGeneratedKey 
  public String getId() {
	return id;
  }

  @DynamoDBAttribute
  public String getFirstName() {
	return firstName;
  }

  @DynamoDBAttribute
  public String getLastName() {
	return lastName;
  }
       
  public void setId(String id) {
    this.id = id;
  }
  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      User user = (User) o;

      return id.equals(user.id);
  }

  @Override
  public int hashCode() {
      return id.hashCode();
  }
}
```

Create a CRUD repository interface in `com.acme.repositories`:

```java
package com.acme.repositories;

@EnableScan
public interface UserRepository extends CrudRepository<User, String> {
  List<User> findByLastName(String lastName);
}
```

or for paging and sorting...

```java
package com.acme.repositories;

public interface UserRepository extends PagingAndSortingRepository<User, String> {
  Page<User> findByLastName(String lastName,Pageable pageable);
  
  @EnableScan 
  @EnableScanCount
  public Page<User> findAll(Pageable pageable);
}
```

And finally write a test client

```java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { 
    PropertyPlaceholderAutoConfiguration.class, DynamoDBConfig.class})
    public class UserRepositoryIntegrationTest {
     
    private static final String KEY_NAME = "id";
    private static final Long READ_CAPACITY_UNITS = 5L;
    private static final Long WRITE_CAPACITY_UNITS = 5L;
    
    @Autowired
    UserRepository repository;
    
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;
    
    @Before
    public void init() throws Exception {
    
        ListTablesResult listTablesResult = amazonDynamoDB.listTables();
    
        listTablesResult.getTableNames().stream().
                filter(tableName -> tableName.equals(User.TABLE_NAME)).forEach(tableName -> {
            amazonDynamoDB.deleteTable(tableName);
        });
    
        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName(KEY_NAME).withAttributeType("S"));
    
        List<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement>();
        keySchemaElements.add(new KeySchemaElement().withAttributeName(KEY_NAME).withKeyType(KeyType.HASH));
    
        CreateTableRequest request = new CreateTableRequest()
                .withTableName(TABLE_NAME)
                .withKeySchema(keySchemaElements)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(READ_CAPACITY_UNITS)
                        .withWriteCapacityUnits(WRITE_CAPACITY_UNITS));
    
        amazonDynamoDB.createTable(request);
    
    }
    
    @Test
    public void sampleTestCase() {
        User dave = new User("Dave", "Matthews");
        repository.save(dave);
    
        User carter = new User("Carter", "Beauford");
        repository.save(carter);
    
        List<User> result = repository.findByLastName("Matthews");
        Assert.assertThat(result.size(), is(1));
        Assert.assertThat(result, hasItem(dave));
    }
}
```
 
 
## Advanced topics ##
Advanced topics can be found in the [wiki](https://github.com/derjust/spring-data-dynamodb/wiki).
