package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.springframework.data.annotation.Id;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "customerhistory")
public class CustomerHistory {
    @Id
    private CustomerHistoryId id;

    private String tag;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "idx_global_tag")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @DynamoDBHashKey(attributeName = "customerId")
    public String getId(){
        return id != null ? id.getCustomerId() : null;
    }

    public void setId(String customerId) {
        if(this.id == null) {
            this.id = new CustomerHistoryId();
        }
        this.id.setCustomerId(customerId);
    }

    @DynamoDBRangeKey(attributeName = "createDt")
    public String getCreateDt() {
        return id != null ? id.getCreateDt() : null;
    }

    public void setCreateDt(String createDt) {
        if(this.id == null) {
            this.id = new CustomerHistoryId();
        }

        this.id.setCreateDt(createDt);
    }
}