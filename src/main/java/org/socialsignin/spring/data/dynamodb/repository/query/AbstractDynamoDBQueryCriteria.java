package org.socialsignin.spring.data.dynamodb.repository.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.socialsignin.spring.data.dynamodb.mapping.DefaultDynamoDBDateMarshaller;
import org.socialsignin.spring.data.dynamodb.query.Query;
import org.socialsignin.spring.data.dynamodb.repository.support.DynamoDBEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
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
	public DynamoDBQueryCriteria<T, ID> withPropertyIn(String propertyName, Iterable<?> value,Class<?> propertyType) {
			
			Condition condition = createCollectionCondition(propertyName, ComparisonOperator.IN, value,propertyType);
			return withCondition(propertyName,condition);	
		}

	@Override
	public DynamoDBQueryCriteria<T, ID> withSingleValueCriteria(String propertyName, ComparisonOperator comparisonOperator,
			Object value,Class<?> propertyType) {
		if (comparisonOperator.equals(ComparisonOperator.EQ))
		{
			return withPropertyEquals(propertyName,value,propertyType);
		}
		else
		{
			Condition condition = createSingleValueCondition(propertyName, comparisonOperator, value,propertyType,false);
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
	
	
	private List<String> getNumberListAsStringList(List<Number> numberList)
	{
		List<String> list = new ArrayList<String>();
		for (Number number : numberList)
		{
			if (number != null)
			{
				list.add(number.toString());
			}
			else
			{
				list.add(null);
			}
		}
		return list;
	}
	
	private List<String> getDateListAsStringList(List<Date> dateList)
	{
		DynamoDBMarshaller<Date> marshaller = new DefaultDynamoDBDateMarshaller();
		List<String> list = new ArrayList<String>();
		for (Date date : dateList)
		{
			if (date != null)
			{
				list.add(marshaller.marshall(date));
			}
			else
			{
				list.add(null);
			}
		}
		return list;
	}
	
	private List<String> getBooleanListAsStringList(List<Boolean> booleanList)
	{
		List<String> list = new ArrayList<String>();
		for (Boolean booleanValue : booleanList)
		{
			if (booleanValue != null)
			{
				list.add(booleanValue.booleanValue() ? "1" : "0");
			}
			else
			{
				list.add(null);
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private <P> List<P> getAttributeValueAsList(Object attributeValue)
	{
		boolean isIterable = ClassUtils.isAssignable(Iterable.class, attributeValue.getClass());
		List<P> attributeValueAsList = null;
		if (isIterable)
		{
			attributeValueAsList = new ArrayList<P>();
			Iterable<P> iterable = (Iterable<P>)attributeValue;
			for (P attributeValueElement : iterable)
			{
				attributeValueAsList.add(attributeValueElement);
			}
			return attributeValueAsList;
		}
		return null;
	}
	
	
	
	
	protected <P> List<AttributeValue> addAttributeValue(List<AttributeValue> attributeValueList,Object attributeValue,String propertyName,Class<P> propertyType,boolean expandCollectionValues)
	{
		AttributeValue attributeValueObject = new AttributeValue();
		
		if (ClassUtils.isAssignable(String.class, propertyType)) {
			List<String> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null)
			{
				attributeValueObject.withSS(attributeValueAsList);
			}
			else
			{
				attributeValueObject.withS((String)attributeValue );
			}
		} else if (ClassUtils.isAssignable(Number.class, propertyType)) {
			
			List<Number> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null)
			{
				List<String> attributeValueAsStringList = getNumberListAsStringList(attributeValueAsList);
				attributeValueObject.withNS(attributeValueAsStringList);
			}
			else
			{
				attributeValueObject.withN(attributeValue.toString());
			}
		} else if (ClassUtils.isAssignable(Boolean.class, propertyType)) {
			List<Boolean> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null)
			{
				List<String> attributeValueAsStringList = getBooleanListAsStringList(attributeValueAsList);
				attributeValueObject.withNS(attributeValueAsStringList);
			}
			else
			{
				boolean boolValue = ((Boolean) attributeValue).booleanValue();
				attributeValueObject.withN(boolValue ? "1" : "0");
			}
		} else if (ClassUtils.isAssignable(Date.class, propertyType)) {
			List<Date> attributeValueAsList = getAttributeValueAsList(attributeValue);
			if (expandCollectionValues && attributeValueAsList != null)
			{
				List<String> attributeValueAsStringList = getDateListAsStringList(attributeValueAsList);
				attributeValueObject.withNS(attributeValueAsStringList);
			}
			else
			{
				Date date = (Date)attributeValue;
				String marshalledDate = new DefaultDynamoDBDateMarshaller().marshall(date);
				attributeValueObject.withS(marshalledDate);
			}
		} else {
			throw new RuntimeException("Cannot create condition for type:" + attributeValue.getClass()
					+ " property conditions must be String,Number or Boolean, or have a DynamoDBMarshaller configured");
		}
		attributeValueList.add(attributeValueObject);
		
		return attributeValueList;
	}
	

	protected Condition createSingleValueCondition(String propertyName,ComparisonOperator comparisonOperator, Object o,Class<?> propertyType,boolean alreadyMarshalledIfRequired) {
				
		Assert.notNull(o,"Creating conditions on null property values not yet supported: please specify a value for '" + propertyName + "'");
		
		Object attributeValue = !alreadyMarshalledIfRequired ? getPropertyAttributeValue(propertyName,o) : o;
		
		boolean marshalled = !alreadyMarshalledIfRequired && attributeValue != o && !entityInformation.isCompositeHashAndRangeKeyProperty(propertyName);
		
		Class<?> targetPropertyType = marshalled ? String.class : propertyType;
		List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>();
		attributeValueList = addAttributeValue(attributeValueList,attributeValue,propertyName,targetPropertyType,true);
		return new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(attributeValueList);
		
	}
	
	protected Condition createCollectionCondition(String propertyName,ComparisonOperator comparisonOperator, Iterable<?> o,Class<?> propertyType) {
		
		Assert.notNull(o,"Creating conditions on null property values not yet supported: please specify a value for '" + propertyName + "'");
		List<AttributeValue> attributeValueList = new ArrayList<AttributeValue>();
		boolean marshalled = false;
		for (Object object : o)
		{
			Object attributeValue = getPropertyAttributeValue(propertyName,object);
			if (attributeValue != null)
			{
				marshalled = attributeValue != object && !entityInformation.isCompositeHashAndRangeKeyProperty(propertyName);
			}
			Class<?> targetPropertyType = marshalled ? String.class : propertyType;
			attributeValueList = addAttributeValue(attributeValueList,attributeValue,propertyName,targetPropertyType,false);

		}

		

		return new Condition().withComparisonOperator(comparisonOperator).withAttributeValueList(attributeValueList);
		
	}

	
	@Override
	public DynamoDBQueryCriteria<T, ID> withSort(Sort sort) {
		this.sort = sort;
		return this;
	}


}
