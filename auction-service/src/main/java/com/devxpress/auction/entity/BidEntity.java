package com.devxpress.auction.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
public class BidEntity {

    private Long itemId;

    private String userId;

    private BigDecimal amount;

    private Instant submittedAt;

}
