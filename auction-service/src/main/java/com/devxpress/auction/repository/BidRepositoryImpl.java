package com.devxpress.auction.repository;

import com.devxpress.auction.entity.BidEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class BidRepositoryImpl implements BidRepository {

    private final Map<Long, List<BidEntity>> ITEM_BID_MAP = new ConcurrentHashMap<>();

    @Override
    public Iterable<BidEntity> findAll() {
        return ITEM_BID_MAP.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<BidEntity> findByItemId(long itemId) {
        return new ArrayList<>(ITEM_BID_MAP.getOrDefault(itemId, Collections.emptyList()));
    }

    @Override
    public Iterable<BidEntity> findByUserId(String userId) {
        return ITEM_BID_MAP.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(bid -> bid.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public BidEntity save(BidEntity bid) {
        ITEM_BID_MAP.compute(bid.getItemId(),
                (k, v) -> {
                    List<BidEntity> vals = v;

                    if (vals == null) {
                        vals = new ArrayList<>();
                    }

                    bid.setSubmittedAt(Instant.now());
                    vals.add(bid);

                    return vals;
                });

        return bid;
    }
}
