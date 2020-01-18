package com.devxpress.auction.api.v1.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @ApiModelProperty(notes = "Unique item identifier")
    private Long id;

    @ApiModelProperty(notes = "Item description", position = 1)
    private String description;

}
