package com.devxpress.auction.repository;

import com.devxpress.auction.entity.BidEntity;

public interface BidRepository {

    Iterable<BidEntity> findAll();

    Iterable<BidEntity> findByItemId(long itemId);

    Iterable<BidEntity> findByUserId(String userId);

    BidEntity save(BidEntity bid);

}
