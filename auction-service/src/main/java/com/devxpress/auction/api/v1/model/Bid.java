package com.devxpress.auction.api.v1.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Bid {

    @ApiModelProperty(notes = "Unique item identifier", required = true)
    private Long itemId;

    @ApiModelProperty(notes = "Item description", position = 1)
    private String itemDescription;

    @ApiModelProperty(notes = "User identifier", required = true, position = 2)
    private String userId;

    @ApiModelProperty(notes = "Bid amount", required = true, position = 3)
    private BigDecimal amount;

    @ApiModelProperty(notes = "Bid creation timestamp (in ISO date/time format for UTC)", position = 4)
    private LocalDateTime createdDateTime;

}
