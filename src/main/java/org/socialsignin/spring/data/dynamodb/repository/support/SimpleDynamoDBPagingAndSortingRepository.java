package org.socialsignin.spring.data.dynamodb.repository.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

/**
 * Default implementation of the
 * {@link org.springframework.data.repository.PagingAndSortingRepository} interface.
 * 
 * Due to DynamoDB limitations, sorting is not supported for find-all operations
 * 
 * Due to DynamoDB limitations, paging for find-all queries is not possible using an integer page number
 * For paged requests, attempt to approximate paging behavior by limiting the number of items which will
 * be scanned, and by returning a sublist of the result-set. 
 *   
 * NB: Number of results scanned for a given page request is proportional to the page number requested!
 * 
 * 
 * @author Michael Lavelle
 * 
 * @param <T>
 *            the type of the entity to handle
 * @param <ID>
 *            the type of the entity's identifier
 */
public class SimpleDynamoDBPagingAndSortingRepository<T,ID extends Serializable> extends SimpleDynamoDBCrudRepository<T, ID> 
implements DynamoDBPagingAndSortingRepository<T,ID>{

	public SimpleDynamoDBPagingAndSortingRepository(DynamoDBEntityInformation<T, ID> entityInformation,
			DynamoDBMapper dynamoDBMapper,EnableScanPermissions enableScanPermissions) {
		super(entityInformation, dynamoDBMapper,enableScanPermissions);
		
	}

	@Override
	public Iterable<T> findAll(Sort sort) {
		throw new UnsupportedOperationException("Sorting not supported for find all scan operations");
	}

	
	@Override
	public Page<T> findAll(Pageable pageable) {
		
		if (pageable.getSort() != null)
		{
			throw new UnsupportedOperationException("Sorting not supported for find all scan operations");
		}
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		// Scan to the end of the page after the requested page
		int scanTo = pageable.getOffset() +  (2 * pageable.getPageSize()) ;
		scanExpression.setLimit(scanTo);
		PaginatedScanList<T> paginatedScanList = dynamoDBMapper.scan(domainType, scanExpression);
		Iterator<T> iterator = paginatedScanList.iterator();
		int processedCount = 0;
		if (pageable.getOffset() > 0)
		{
			processedCount = scanThroughResults(iterator,pageable.getOffset());
			if (processedCount < pageable.getOffset()) return new PageImpl<T>(new ArrayList<T>());
		}
		// Scan ahead to retrieve the next page count
		List<T> results = readPageOfResults(iterator,pageable.getPageSize());
		int nextPageItemCount = scanThroughResults(iterator,pageable.getPageSize());
		boolean hasMoreResults = nextPageItemCount > 0;
		int totalProcessed = processedCount + results.size();
		// Set total count to be the number already returned, or the number returned added to the count of the next page
		// This allows paging to determine next/page prev page correctly, even though we are unable to return
		// the actual count of total results due to the way DynamoDB scans results
		return new PageImpl<T>(results,pageable,hasMoreResults ? (totalProcessed + nextPageItemCount) : totalProcessed);
		
	}
	
	private int scanThroughResults(Iterator<T> paginatedScanListIterator,int resultsToScan)
	{
		int processed = 0;
		while (paginatedScanListIterator.hasNext() && processed < resultsToScan)
		{
			paginatedScanListIterator.next();
			processed++;
		}
		return processed;
	}
	
	private List<T> readPageOfResults(Iterator<T> paginatedScanListIterator,int pageSize)
	{
		int processed = 0;
		List<T> resultsPage = new ArrayList<T>();
		while (paginatedScanListIterator.hasNext() && processed < pageSize)
		{
			resultsPage.add(paginatedScanListIterator.next());
			processed++;
		}
		return resultsPage;
	}
	
	
	

}
