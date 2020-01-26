package com.devxpress.auction.repository;

import com.devxpress.auction.api.exception.InvalidResourceException;
import com.devxpress.auction.entity.BidEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class BidRepositoryImpl implements BidRepository {

    private final Map<Long, LinkedList<BidEntity>> ITEM_BID_MAP = new ConcurrentHashMap<>();

    @Override
    public Iterable<BidEntity> findAll() {
        return ITEM_BID_MAP.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<BidEntity> findByItemId(long itemId) {
        return new ArrayList<>(ITEM_BID_MAP.getOrDefault(itemId, new LinkedList<>()));
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
                    LinkedList<BidEntity> vals = v;

                    if (vals == null) {
                        vals = new LinkedList<>();

                        bid.setSubmittedAt(Instant.now());
                        vals.addFirst(bid);
                    } else {
                        // Valid bid must be for an amount greater than current highest
                        // which is the first bid in the linked list
                        if (bid.getAmount().compareTo(vals.getFirst().getAmount()) > 0) {
                            bid.setSubmittedAt(Instant.now());
                            vals.addFirst(bid);
                        } else {
                            String msg = String.format(
                                    "Invalid bid on item : %s, for user : %s", bid.getItemId(), bid.getUserId());
                            InvalidResourceException ire = new InvalidResourceException(msg);
                            ire.addReason("Amount bid must be greater than current highest");
                            throw ire;
                        }
                    }

                    return vals;
                });

        return bid;
    }
}
