package com.devxpress.auction.service;

import com.devxpress.auction.api.v1.model.Bid;

import java.util.List;

public interface BidService {

    List<Bid> getAllBids();

    List<Bid> getBidsForItem(long itemId);

    List<Bid> getBidsForUser(String userId);

    Bid createBid(Bid bid);

}
