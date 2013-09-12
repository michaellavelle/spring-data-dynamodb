# Spring Data DynamoDB ( Work in Progress ) #

The primary goal of the [Spring Data](http://www.springsource.org/spring-data) project is to make it easier to build Spring-powered applications that use data access technologies. This module deals with enhanced support for Amazon DynamoDB based data access layers.

## Supported Features ##

* Implementation of CRUD methods for DynamoDB Entities
* Dynamic query generation from query method names  (Only a limited number of keywords and comparison operators currently supported)
* Implementation domain base classes providing basic properties
* Possibility to integrate custom repository code
* Easy Spring annotation based integration

## Quick Start ##

Download the jar though Maven:


```xml
<repository>
	<id>opensourceagility-snapshots</id>
	<url>http://repo.opensourceagility.com/snapshots</url
</repository>
```

```xml
<dependency>
  <groupId>org.socialsignin</groupId>
  <artifactId>spring-data-dynamodb</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
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
	public AmazonDynamoDB amazonDynamoDB() {
		AmazonDynamoDB amazonDynamoDB = new AmazonDynamoDBClient(
				amazonAWSCredentials());
		if (StringUtils.isNotEmpty(amazonDynamoDBEndpoint)) {
			amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
		}
		return amazonDynamoDB;
	}

	@Bean
	public AWSCredentials amazonAWSCredentials() {
		return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
	}

}
```

Create a DynamoDB entity:

```java
@DynamoDBTable(tableName = "User")
public class User {

  private String id;
  private String firstName;
  private String lastName;

  @DynamoDBHashKey
  public String getId()
  {
	return id;
  }

  @DynamoDBAttribute
  public String getFirstName()
  {
	return firstname;
  }

  @DynamoDBAttribute
  public String getLastName()
  {
	return lastName;
  }
       
  // setters
}
```

Create a repository interface in `com.acme.repositories`:

```java
public interface UserRepository extends CrudRepository<User, Long> {
  List<User> findByLastName(String lastname);
}
```

Write a test client

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:your-config-file.xml")
public class UserRepositoryIntegrationTest {
     
  @Autowired UserRepository repository;
     
  @Test
  public void sampleTestCase() {
    User dave = new User("Dave", "Matthews");
    repository.save(user);
         
    User carter = new User("Carter", "Beauford");
    repository.save(carter);
         
    List<User> result = repository.findByLastName("Matthews");
    assertThat(result.size(), is(1));
    assertThat(result, hasItem(dave));
  }
}
```


