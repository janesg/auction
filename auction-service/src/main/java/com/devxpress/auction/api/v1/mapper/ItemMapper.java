package com.devxpress.auction.api.v1.mapper;

import com.devxpress.auction.api.v1.model.Item;
import com.devxpress.auction.entity.ItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ItemMapper {

    Item itemEntityToItem(ItemEntity item);

}
