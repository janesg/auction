package com.devxpress.auction.controller;

import com.devxpress.auction.api.ApiError;
import com.devxpress.auction.api.exception.BaseException;
import com.devxpress.auction.api.exception.InvalidResourceException;
import com.devxpress.auction.api.exception.ResourceCrudException;
import com.devxpress.auction.api.exception.ResourceNotFoundException;
import com.devxpress.auction.api.v1.model.Bid;
import com.devxpress.auction.api.v1.model.BidDetail;
import com.devxpress.auction.api.v1.model.Item;
import com.devxpress.auction.service.BidService;
import com.devxpress.auction.service.ItemService;
import com.devxpress.auction.utils.ControllerUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.devxpress.auction.api.ApiErrorCode.UNEXPECTED_ERROR;
import static com.devxpress.auction.api.ApiErrorMessage.INVALID_RESOURCE_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_NOT_FOUND_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_OPERATION_ERROR_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.SYSTEM_ERROR_MSG;
import static com.devxpress.auction.utils.ExceptionUtils.getMessage;

@RestController
@Slf4j
@RequiredArgsConstructor
@Api(value = "item")
public class AuctionController {

    static final String EMPTY_ITEM_ID = "Item id must be specified";
    static final String INVALID_ITEM_ID_FORMAT = "Item id : %s, does not conform to the required format";
    static final String INVALID_ITEM_ID = "Item id : %s, is invalid";
    static final String ITEM_ID_MISMATCH = "Item id mismatch between request path and body";
    static final String ITEM_NOT_EXIST = "Item id : %s, does not relate to an existing item";
    static final String WINNING_BID_NOT_FOUND = "No winning bid for item id : %s";
    static final String BID_CREATION_FAILED = "Failed to create bid : ";
    static final String EMPTY_USER_ID = "User id must be specified";
    static final String EMPTY_AMOUNT = "Amount must be specified";
    static final String INVALID_AMOUNT = "Amount must be greater than zero";

    private static final String BID_CREATED = "Successfully submitted a bid";

    private final ItemService itemService;
    private final BidService bidService;

    // Example URL:
    //      http://localhost:8080/v1/items
    @GetMapping(value = "/v1/items", produces = "application/json")
    @ApiOperation(value = "Return a set of auction items",
            notes = "Retrieve auction items",
            response = Item.class,
            responseContainer = "Set")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of auction items",
                    response = Item.class, responseContainer = "Set"),
            @ApiResponse(code = 500, message = SYSTEM_ERROR_MSG, response = ApiError.class)
    })
    public ResponseEntity<Set<Item>> getItems(
            @ApiParam(value = "'Items bid on by' user identifier")
            @RequestParam(value = "bid-user-id", required = false) String bidUserId) {

        log.info(bidUserId == null ? "Retrieving all auction items" :
                 String.format("Retrieving auctions items on which user : %s, has bid", bidUserId));

        try {
            Set<Item> items;

            if (bidUserId == null) {
                items = itemService.getAllItems();
            } else {
                items = bidService.getBidsForUser(bidUserId).stream()
                        .map(BidDetail::getItemId)
                        .map(itemService::getItem)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            log.info("Retrieved {} auction item(s)", items.size());
            return new ResponseEntity<>(items, HttpStatus.OK);
        } catch (Exception e) {
            String msg = (bidUserId == null ?
                    String.format("Failed to retrieve all auction items - %s", getMessage(e)) :
                    String.format("Failed to retrieve auctions items on which user : %s, has bid - %s",
                            bidUserId, getMessage(e)));
            log.error(msg);
            throw new BaseException(msg, UNEXPECTED_ERROR);
        }
    }

    // Example URL:
    //      http://localhost:8080/v1/items/4/bids
    @GetMapping(value = "/v1/items/{item-id}/bids", produces = "application/json")
    @ApiOperation(value = "Return all bids for an auction item",
            notes = "Retrieve all bids for a specified auction item",
            response = BidDetail.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of item bids",
                    response = BidDetail.class, responseContainer = "List"),
            @ApiResponse(code = 422, message = INVALID_RESOURCE_MSG, response = ApiError.class),
            @ApiResponse(code = 500, message = SYSTEM_ERROR_MSG, response = ApiError.class)
    })
    public ResponseEntity<List<BidDetail>> getAllBidsForItem(
            @ApiParam(value = "Item identifier", required = true)
            @PathVariable("item-id") String itemIdStr) {

        Long itemId = ControllerUtils.convertStringToResourceId(itemIdStr, EMPTY_ITEM_ID,
                String.format(INVALID_ITEM_ID_FORMAT, itemIdStr));

        log.info(String.format("Retrieving all bids on auction item with id : %s", itemId));

        if (!isValidItemId(itemId)) {
            String msg = String.format(INVALID_ITEM_ID, itemId);
            log.error(msg);
            throw new InvalidResourceException(msg);
        }

        try {
            List<BidDetail> bids = bidService.getBidsForItem(itemId);
            bids.forEach(b -> b.setItemDescription(itemService.getItem(b.getItemId()).getDescription()));

            log.info(String.format("Retrieved %s bids on auction item with id : %s", bids.size(), itemId));
            return new ResponseEntity<>(bids, HttpStatus.OK);
        } catch (Exception e) {
            String msg = String.format("Failed to retrieve bids on auction item with id : %s - %s",
                            itemId, getMessage(e));
            log.error(msg);
            throw new BaseException(msg, UNEXPECTED_ERROR);
        }
    }

    // Example URL:
    //      http://localhost:8080/v1/items/4/bids/winning
    @GetMapping(value = "/v1/items/{item-id}/bids/winning", produces = "application/json")
    @ApiOperation(value = "Return the current winning bid for an auction item",
            notes = "Retrieve the current winning bid for a specified auction item",
            response = BidDetail.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of winning item bid",
                    response = BidDetail.class),
            @ApiResponse(code = 404, message = RESOURCE_NOT_FOUND_MSG, response = ApiError.class),
            @ApiResponse(code = 500, message = SYSTEM_ERROR_MSG, response = ApiError.class)
    })
    public ResponseEntity<BidDetail> getWinningBidForItem(
            @ApiParam(value = "Item identifier", required = true)
            @PathVariable("item-id") String itemIdStr) {

        Long itemId = ControllerUtils.convertStringToResourceId(itemIdStr, EMPTY_ITEM_ID,
                String.format(INVALID_ITEM_ID_FORMAT, itemIdStr));

        log.info(String.format("Retrieving winning bid on auction item with id : %s", itemId));

        if (!isValidItemId(itemId)) {
            String msg = String.format(INVALID_ITEM_ID, itemId);
            log.error(msg);
            throw new InvalidResourceException(msg);
        }

        try {
            // Note: as the stream is ordered (such as the streams you get from an array or List),
            //       the first element that is maximal is returned in the event of multiple maximal elements

            // TODO : rather than return first maximal element, we should perhaps
            //        return the bid that was submitted first !
            BidDetail winningBid = bidService.getBidsForItem(itemId).stream()
                    .max(Comparator.comparing(BidDetail::getAmount))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format(WINNING_BID_NOT_FOUND, itemId)));

            winningBid.setItemDescription(itemService.getItem(winningBid.getItemId()).getDescription());

            log.info(String.format("Retrieved winning bid on auction item with id : %s", itemId));
            return new ResponseEntity<>(winningBid, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            log.error(getMessage(e));
            throw e;
        } catch (Exception e) {
            String msg = String.format("Failed to retrieve winning bid on auction item with id : %s - %s",
                    itemId, getMessage(e));
            log.error(msg);
            throw new BaseException(msg, UNEXPECTED_ERROR);
        }
    }

    @PostMapping(value = "/v1/items/{item-id}/bids", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Create a new bid",
            notes = "Submit a new bid for an item",
            response = BidDetail.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = BID_CREATED, response = BidDetail.class),
            @ApiResponse(code = 409, message = RESOURCE_OPERATION_ERROR_MSG, response = ApiError.class),
            @ApiResponse(code = 422, message = INVALID_RESOURCE_MSG, response = ApiError.class),
            @ApiResponse(code = 500, message = SYSTEM_ERROR_MSG, response = ApiError.class)
    })
    public ResponseEntity<BidDetail> createBid(
            @ApiParam(value = "Item identifier", required = true)
            @PathVariable("item-id") String itemIdStr,
            @Valid @RequestBody Bid bid) {

        Long itemId = ControllerUtils.convertStringToResourceId(itemIdStr, EMPTY_ITEM_ID,
                String.format(INVALID_ITEM_ID_FORMAT, itemIdStr));

        log.info("Creating new bid on item : {}, for user : {}", bid.getItemId(), bid.getUserId());

        if (!itemId.equals(bid.getItemId())) {
            throw new IllegalArgumentException(ITEM_ID_MISMATCH);
        }

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
            BidDetail createdBidDetail = bidService.createBid(bid);
            createdBidDetail.setItemDescription(
                    itemService.getItem(createdBidDetail.getItemId()).getDescription());

            log.info("Created a new bid on item : {}, for user : {}",
                    createdBidDetail.getItemId(), createdBidDetail.getUserId());

            return new ResponseEntity<>(createdBidDetail, HttpStatus.CREATED);
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

            // TODO : if each item were to have a timestamp indicating
            //        when the auction on that item finishes we can
            //        validate whether the bid is for an item for which the
            //        auction has already ended
        }

        if (bid.getUserId() == null) {
            errors.add(EMPTY_USER_ID);
        }

        if (bid.getAmount() == null) {
            errors.add(EMPTY_AMOUNT);
        } else if (bid.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(INVALID_AMOUNT);
        } else {
            // TODO : Check that the amount bid is higher than any previous
            //        bid made on same item by same user
        }

        return errors;
    }

    private boolean isValidItemId(Long itemId) {
        try {
            itemService.getItem(itemId);
        } catch (ResourceNotFoundException e) {
            return false;
        }

        return true;
    }
}
