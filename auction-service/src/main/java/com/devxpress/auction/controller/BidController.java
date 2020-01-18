package com.devxpress.auction.controller;

import com.devxpress.auction.api.ApiError;
import com.devxpress.auction.api.exception.InvalidResourceException;
import com.devxpress.auction.api.exception.ResourceCrudException;
import com.devxpress.auction.api.exception.ResourceNotFoundException;
import com.devxpress.auction.api.v1.model.Bid;
import com.devxpress.auction.service.BidService;
import com.devxpress.auction.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.devxpress.auction.api.ApiErrorMessage.INVALID_RESOURCE_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_OPERATION_ERROR_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.SYSTEM_ERROR_MSG;
import static com.devxpress.auction.utils.ExceptionUtils.getMessage;

@RestController
@Slf4j
@RequiredArgsConstructor
@Api(value = "bid")
public class BidController {

    static final String BID_CREATION_FAILED = "Failed to create bid : ";
    static final String EMPTY_ITEM_ID = "Item id must be specified";
    static final String EMPTY_USER_ID = "User id must be specified";
    static final String EMPTY_AMOUNT = "Amount must be specified";
    static final String INVALID_AMOUNT = "Amount must be greater than zero";
    static final String ITEM_NOT_EXIST = "Item id : %s, does not relate to an existing item";

    private static final String BID_CREATED = "Successfully submitted a bid";

    private final BidService bidService;
    private final ItemService itemService;

    @PostMapping(value = "/v1/bids", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Create a new bid",
            notes = "Submit a new bid for an item",
            response = Bid.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = BID_CREATED, response = Bid.class),
            @ApiResponse(code = 409, message = RESOURCE_OPERATION_ERROR_MSG, response = ApiError.class),
            @ApiResponse(code = 422, message = INVALID_RESOURCE_MSG, response = ApiError.class),
            @ApiResponse(code = 500, message = SYSTEM_ERROR_MSG, response = ApiError.class)
    })
    public ResponseEntity<Bid> createPayment(@Valid @RequestBody Bid bid) {

        log.info("Creating new bid on item : {}, for user : {}", bid.getItemId(), bid.getUserId());

        List<String> errors = validateBid(bid);

        if (!errors.isEmpty()) {
            String msg = String.format(
                    "Invalid bid on item : %s, for user : %s", bid.getItemId(), bid.getUserId());
            log.error(msg);
            InvalidResourceException ire = new InvalidResourceException(msg);
            errors.forEach(ire::addReason);
            throw ire;
        }

        try {
            Bid createdBid = bidService.createBid(bid);
            createdBid.setItemDescription(
                    itemService.getItem(createdBid.getItemId()).getDescription());

            log.info("Created a new bid on item : {}, for user : {}",
                    createdBid.getItemId(), createdBid.getUserId());

            return new ResponseEntity<>(createdBid, HttpStatus.CREATED);
        } catch (Exception e) {
            String msg = BID_CREATION_FAILED + getMessage(e);
            log.error(msg);
            throw new ResourceCrudException(msg);
        }
    }

    private List<String> validateBid(Bid bid) {
        List<String> errors = new ArrayList<>();

        if (bid.getItemId() == null) {
            errors.add(EMPTY_ITEM_ID);
        } else {
            try {
                itemService.getItem(bid.getItemId());
            } catch (ResourceNotFoundException e) {
                errors.add(String.format(ITEM_NOT_EXIST, bid.getItemId()));
            }
        }

        if (bid.getUserId() == null) {
            errors.add(EMPTY_USER_ID);
        }

        if (bid.getAmount() == null) {
            errors.add(EMPTY_AMOUNT);
        } else if (bid.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(INVALID_AMOUNT);
        }

        return errors;
    }
}
