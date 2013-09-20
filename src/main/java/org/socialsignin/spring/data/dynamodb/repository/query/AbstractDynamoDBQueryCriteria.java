package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.mapping.DefaultDynamoDBDateMarshaller;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

public abstract class AbstractDynamoDBQueryCriteria<T,ID extends Serializable> implements DynamoDBQueryCriteria<T,ID> {

	protected Class<T> clazz;
	private DynamoDBEntityInformation<T,ID> entityInformation;
	private Map<String,String> attributeNamesByPropertyName;
	private String hashKeyPropertyName;
	
	protected MultiValueMap<String,Condition> attributeConditions;
	protected Object hashKeyAttributeValue;
	protected Object hashKeyPropertyValue;
	protected Sort sort;

	
	
	public abstract boolean isApplicableForLoad();

	
	public AbstractDynamoDBQueryCriteria(DynamoDBEntityInformation<T,ID> dynamoDBEntityInformation)
	{
		this.clazz = dynamoDBEntityInformation.getJavaType();
		this.attributeConditions = new LinkedMultiValueMap<String,Condition>();
		this.hashKeyPropertyName = dynamoDBEntityInformation.getHashKeyPropertyName();
		this.entityInformation = dynamoDBEntityInformation;
		this.attributeNamesByPropertyName = new HashMap<String,String>();
		
	}

	protected boolean isHashKeyProperty(String propertyName)
	{
		return hashKeyPropertyName.equals(propertyName);
	}
	
	protected String getHashKeyPropertyName()
	{
		return hashKeyPropertyName;
	}
	
	protected String getHashKeyAttributeName()
	{
		return getAttributeName(getHashKeyPropertyName());
	}
	
	public DynamoDBQueryCriteria<T, ID> withHashKeyEquals(Object value)
	{
		hashKeyAttributeValue = getPropertyAttributeValue(getHashKeyPropertyName(),value);
		hashKeyPropertyValue = value;
		return this;
	}
	

	public boolean isHashKeySpecified()
	{
		return getHashKeyAttributeValue() != null;
	}

	public Object getHashKeyAttributeValue() {
		return hashKeyAttributeValue;
	}
	
	public Object getHashKeyPropertyValue() {
		return hashKeyPropertyValue;
	}
	
	


	protected String getAttributeName(String propertyName)
	{
		String attributeName = attributeNamesByPropertyName.get(propertyName);
		if (attributeName == null)
		{
			String overriddenName = entityInformation.getOverriddenAttributeName(propertyName);
			attributeName = overriddenName != null ? overriddenName : propertyName;
			attributeNamesByPropertyName.put(propertyName, attributeName);
		}
		return attributeName;
		
	}
	
	

	@Override
	public DynamoDBQueryCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value) {
		if (comparisonOperator.equals(ComparisonOperator.EQ))
		{
			return withPropertyEquals(propertyName,value);
		}
		else
		{
			Condition condition = createSingleValueCondition(propertyName, comparisonOperator, value);
			return withCondition(propertyName,condition);
		}
	}

	@Override
	public Query<T> buildQuery(DynamoDBMapper dynamoDBMapper) {
		if (isApplicableForLoad())
		{
			return buildSingleEntityLoadQuery(dynamoDBMapper);
		}
		else 
		{
			return buildFinderQuery(dynamoDBMapper);
		}
	}

	protected abstract Query<T> buildSingleEntityLoadQuery(DynamoDBMapper dynamoDBMapper);
	protected abstract Query<T> buildFinderQuery(DynamoDBMapper dynamoDBMapper);

	protected abstract boolean isOnlyHashKeySpecified();
	
	@Override
	public DynamoDBQueryCriteria<T, ID> withNoValuedCriteria(String propertyName, ComparisonOperator comparisonOperator) {
		Condition condition = createNoValueCondition(propertyName, comparisonOperator);
		return withCondition(propertyName,condition);

	}

	public DynamoDBQueryCriteria<T, ID> withCondition(String propertyName,Condition condition)
	{
		attributeConditions.add(getAttributeName(propertyName), condition);
		return this;
	}
	


	
	
	@SuppressWarnings("unchecked")
	protected <V> Object getPropertyAttributeValue(String propertyName,Object value)
	{
		DynamoDBMarshaller<V> marshaller =  (DynamoDBMarshaller<V>) entityInformation.getMarshallerForProperty(propertyName);
		
		if (marshaller != null)
		{
			return marshaller.marshall((V)value);
		}
		else
		{
			return value;
		}
	}
	
	
	protected <V> Condition createNoValueCondition(String propertyName,ComparisonOperator comparisonOperator) {
		
		Condition condition = new Condition().withComparisonOperator(comparisonOperator);

		return condition;
	}

	protected Condition createSingleValueCondition(String propertyName,ComparisonOperator comparisonOperator, Object o) {
				
		Assert.notNull(o,"Creating conditions on null property values not yet supported: please specify a value for '" + propertyName + "'");
		
		Object attributeValue = getPropertyAttributeValue(propertyName,o);
	
		if (attributeValue instanceof String) {
			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withS((String)attributeValue ));

			return condition;

		} else if (attributeValue instanceof Number) {

			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withN(attributeValue.toString()));
			return condition;
		} else if (attributeValue instanceof Boolean) {
			boolean boolValue = ((Boolean) attributeValue).booleanValue();
			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withN(boolValue ? "1" : "0"));
			return condition;
		} else if (attributeValue instanceof Date) {
			Date date = (Date)attributeValue;
			String marshalledDate = new DefaultDynamoDBDateMarshaller().marshall(date);

			Condition condition = new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(
					new AttributeValue().withS(marshalledDate));
			return condition;
		} else {
			throw new RuntimeException("Cannot create condition for type:" + attributeValue.getClass()
					+ " property conditions must be String,Number or Boolean, or have a DynamoDBMarshaller configured");
		}
	}

	
	@Override
	public DynamoDBQueryCriteria<T, ID> withSort(Sort sort) {
		this.sort = sort;
		return this;
	}


}
