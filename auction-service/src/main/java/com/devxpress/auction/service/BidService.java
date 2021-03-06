package com.devxpress.auction.service;

import com.devxpress.auction.api.v1.model.Bid;
import com.devxpress.auction.api.v1.model.BidDetail;

import java.util.List;
import java.util.Optional;

public interface BidService {

    List<BidDetail> getAllBids();

    List<BidDetail> getBidsForItem(long itemId);

    Optional<BidDetail> getWinningBidForItem(long itemId);

    List<BidDetail> getBidsForUser(String userId);

    BidDetail createBid(Bid bid);

}
