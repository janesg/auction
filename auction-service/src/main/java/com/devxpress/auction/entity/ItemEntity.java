package com.devxpress.auction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemEntity {

    private Long id;

    private String description;

    private String category;

    // TODO : as an auction item we should also include a timestamp
    //        representing when the auction for the item finishes

}
