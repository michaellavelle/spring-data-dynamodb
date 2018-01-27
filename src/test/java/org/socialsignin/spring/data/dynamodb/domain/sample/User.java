/**
 * Copyright © 2013 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb)
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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.socialsignin.spring.data.dynamodb.marshaller.Instant2IsoDynamoDBMarshaller;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

@DynamoDBTable(tableName = "user")
public class User {

	private String id;
	
	private String name;
	
	private Integer numberOfPlaylists;
	
	private Date joinDate;

	@DynamoDBMarshalling(marshallerClass=DynamoDBYearMarshaller.class)
	private Date joinYear;
	
	private Instant leaveDate;
	
	private String postCode;
	
	private Set<String> testSet;

	public Set<String> getTestSet() {
		return testSet;
	}

	public void setTestSet(Set<String> testSet) {
		this.testSet = testSet;
	}

	
	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}
	
	public Date getJoinYear() {
		return joinYear;
	}

	public void setJoinYear(Date joinYear) {
		this.joinYear = joinYear;
	}
	
	@DynamoDBMarshalling(marshallerClass=Instant2IsoDynamoDBMarshaller.class)
	public Instant getLeaveDate() {
		return leaveDate;
	}
	
	public void setLeaveDate(Instant leaveDate) {
		this.leaveDate = leaveDate;
	}
	
	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	@DynamoDBHashKey(attributeName = "Id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumberOfPlaylists() {
		return numberOfPlaylists;
	}

	public void setNumberOfPlaylists(Integer numberOfPlaylists) {
		this.numberOfPlaylists = numberOfPlaylists;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((joinDate == null) ? 0
		        : joinDate.hashCode());
		result = prime * result + ((joinYear == null) ? 0
		        : joinYear.hashCode());
		result = prime * result + ((leaveDate == null) ? 0
		        : leaveDate.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((numberOfPlaylists == null) ? 0
		        : numberOfPlaylists.hashCode());
		result = prime * result + ((postCode == null) ? 0
		        : postCode.hashCode());
		result = prime * result + ((testSet == null) ? 0 : testSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (joinDate == null) {
			if (other.joinDate != null)
				return false;
		} else if (!joinDate.equals(other.joinDate))
			return false;
		if (joinYear == null) {
			if (other.joinYear != null)
				return false;
		} else if (!joinYear.equals(other.joinYear))
		if (leaveDate == null) {
			if (other.leaveDate != null)
				return false;
		} else if (!leaveDate.equals(other.leaveDate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (numberOfPlaylists == null) {
			if (other.numberOfPlaylists != null)
				return false;
		} else if (!numberOfPlaylists.equals(other.numberOfPlaylists))
			return false;
		if (postCode == null) {
			if (other.postCode != null)
				return false;
		} else if (!postCode.equals(other.postCode))
			return false;
		if (testSet == null) {
			if (other.testSet != null)
				return false;
		} else if (!testSet.equals(other.testSet))
			return false;
		return true;
	}

}
