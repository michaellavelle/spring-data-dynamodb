package org.socialsignin.spring.data.dynamodb.repository.support;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.socialsignin.spring.data.dynamodb.domain.sample.Playlist;
import org.socialsignin.spring.data.dynamodb.domain.sample.PlaylistId;
import org.socialsignin.spring.data.dynamodb.domain.sample.User;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unused")
public class DynamoDBEntityMetadataSupportUnitTest {
	
	@Test
	public void testGetMarshallerForProperty_WhenAnnotationIsOnField_AndReturnsDynamoDBMarshaller()
	{
		DynamoDBEntityMetadataSupport support = new DynamoDBEntityMetadataSupport(User.class);
		DynamoDBMarshaller fieldAnnotation = support.getMarshallerForProperty("joinYear");
		Assert.assertNotNull(fieldAnnotation);
	}

	@Test
	public void testGetMarshallerForProperty_WhenAnnotationIsOnMethod_AndReturnsDynamoDBMarshaller()
	{
		DynamoDBEntityMetadataSupport support = new DynamoDBEntityMetadataSupport(User.class);
		DynamoDBMarshaller methodAnnotation = support.getMarshallerForProperty("leaveDate");
		Assert.assertNotNull(methodAnnotation);
	}
}
