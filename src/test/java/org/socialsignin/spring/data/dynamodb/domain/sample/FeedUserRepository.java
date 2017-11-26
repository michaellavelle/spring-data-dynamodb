package org.socialsignin.spring.data.dynamodb.domain.sample;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedUserRepository extends DynamoDBPagingAndSortingRepository<FeedUser, String>{
    public List<FeedUser> findByUsrNo(int usrNo, Pageable pageable);
    public List<FeedUser> findByUsrNoAndFeedOpenYn(int usrNo, boolean feedOpenYn, Pageable pageable);
}