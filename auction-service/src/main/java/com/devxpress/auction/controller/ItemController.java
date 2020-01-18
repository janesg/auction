package com.devxpress.auction.controller;

import com.devxpress.auction.api.ApiError;
import com.devxpress.auction.api.exception.BaseException;
import com.devxpress.auction.api.exception.InvalidResourceException;
import com.devxpress.auction.api.exception.ResourceNotFoundException;
import com.devxpress.auction.api.v1.model.Bid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.devxpress.auction.api.ApiErrorCode.UNEXPECTED_ERROR;
import static com.devxpress.auction.api.ApiErrorMessage.INVALID_RESOURCE_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_NOT_FOUND_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.SYSTEM_ERROR_MSG;
import static com.devxpress.auction.utils.ExceptionUtils.getMessage;

@RestController
@Slf4j
@RequiredArgsConstructor
@Api(value = "item")
public class ItemController {

    static final String EMPTY_ITEM_ID = "Item id must be specified";
    static final String INVALID_ITEM_ID_FORMAT = "Item id : %s, does not conform to the required format";
    static final String INVALID_ITEM_ID = "Item id : %s, is invalid";
    static final String WINNING_BID_NOT_FOUND = "No winning bid for item id : %s";

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
                        .map(Bid::getItemId)
                        .map(itemService::getItem)
                        .collect(Collectors.toSet());
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
            response = Bid.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of item bids",
                    response = Bid.class, responseContainer = "List"),
            @ApiResponse(code = 422, message = INVALID_RESOURCE_MSG, response = ApiError.class),
            @ApiResponse(code = 500, message = SYSTEM_ERROR_MSG, response = ApiError.class)
    })
    public ResponseEntity<List<Bid>> getAllBidsForItem(
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
            List<Bid> bids = bidService.getBidsForItem(itemId);
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
            response = Bid.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of winning item bid", response = Bid.class),
            @ApiResponse(code = 404, message = RESOURCE_NOT_FOUND_MSG, response = ApiError.class),
            @ApiResponse(code = 500, message = SYSTEM_ERROR_MSG, response = ApiError.class)
    })
    public ResponseEntity<Bid> getWinningBidForItem(
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
            Bid winningBid = bidService.getBidsForItem(itemId).stream()
                    .max(Comparator.comparing(Bid::getAmount))
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

    private boolean isValidItemId(Long itemId) {
        try {
            itemService.getItem(itemId);
        } catch (ResourceNotFoundException e) {
            return false;
        }

        return true;
    }
}
