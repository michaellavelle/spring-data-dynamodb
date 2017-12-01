package org.socialsignin.spring.data.dynamodb.domain.sample;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.io.Serializable;
import java.util.Date;


/**
 * Test for <a href="https://github.com/derjust/spring-data-dynamodb/issues/52">Issue 52</a>.
 */
@DynamoDBTable(tableName = "installations")
public class Installation implements Serializable {
    private static final long serialVersionUID = 1L;

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    public String id;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "idx-global-systemid")
    private String systemId;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "idx-global-systemid")
    private Date updatedAt;


    public Installation() {

    }

    public Installation(final String systemId, final Date updatedAt) {
        this.systemId = systemId;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(final String systemId) {
        this.systemId = systemId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Installation [id='" + id + "', systemId='" + systemId + "', updatedAt='" + updatedAt + "']";
    }
}

