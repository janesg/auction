package com.devxpress.auction.service;

import com.devxpress.auction.api.v1.mapper.BidMapper;
import com.devxpress.auction.api.v1.model.Bid;
import com.devxpress.auction.api.v1.model.BidDetail;
import com.devxpress.auction.entity.BidEntity;
import com.devxpress.auction.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final BidMapper bidMapper;

    @Override
    public List<BidDetail> getAllBids() {

        Iterable<BidEntity> bids = bidRepository.findAll();

        return StreamSupport
                .stream(bids.spliterator(), false)
                .map(bidMapper::bidEntityToBidDetail)
                .collect(Collectors.toList());
    }

    @Override
    public List<BidDetail> getBidsForItem(long itemId) {

        Iterable<BidEntity> bids = bidRepository.findByItemId(itemId);

        return StreamSupport
                .stream(bids.spliterator(), false)
                .map(bidMapper::bidEntityToBidDetail)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BidDetail> getWinningBidForItem(long itemId) {
        Iterable<BidEntity> bids = bidRepository.findByItemId(itemId);

        return StreamSupport
                .stream(bids.spliterator(), false)
                .findFirst()
                .map(bidMapper::bidEntityToBidDetail);
    }

    @Override
    public List<BidDetail> getBidsForUser(String userId) {

        Iterable<BidEntity> bids = bidRepository.findByUserId(userId);

        return StreamSupport
                .stream(bids.spliterator(), false)
                .map(bidMapper::bidEntityToBidDetail)
                .collect(Collectors.toList());
    }

    @Override
    public BidDetail createBid(Bid bid) {
        return bidMapper.bidEntityToBidDetail(
                bidRepository.save(bidMapper.bidToBidEntity(bid)));
    }
}
