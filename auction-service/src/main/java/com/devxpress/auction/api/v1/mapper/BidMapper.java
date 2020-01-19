package com.devxpress.auction.api.v1.mapper;

import com.devxpress.auction.api.v1.model.Bid;
import com.devxpress.auction.api.v1.model.BidDetail;
import com.devxpress.auction.entity.BidEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface BidMapper {

    @Mappings({
            @Mapping(source = "submittedAt", target = "createdDateTime")
    })
    BidDetail bidEntityToBidDetail(BidEntity bidEntity);

    BidEntity bidToBidEntity(Bid bid);

    default LocalDateTime instantToLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
