package com.devxpress.auction.controller;

import com.devxpress.auction.api.exception.ResourceNotFoundException;
import com.devxpress.auction.api.v1.model.Bid;
import com.devxpress.auction.api.v1.model.BidDetail;
import com.devxpress.auction.api.v1.model.Item;
import com.devxpress.auction.service.BidService;
import com.devxpress.auction.service.ItemService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.devxpress.auction.api.ApiErrorCode.UNEXPECTED_ERROR;
import static com.devxpress.auction.api.ApiErrorMessage.INVALID_RESOURCE_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.MISSING_OR_INVALID_ARGUMENT_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_NOT_FOUND_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_OPERATION_ERROR_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.SYSTEM_ERROR_MSG;
import static com.devxpress.auction.controller.AuctionController.BID_CREATION_FAILED;
import static com.devxpress.auction.controller.AuctionController.EMPTY_AMOUNT;
import static com.devxpress.auction.controller.AuctionController.EMPTY_USER_ID;
import static com.devxpress.auction.controller.AuctionController.INVALID_AMOUNT;
import static com.devxpress.auction.controller.AuctionController.INVALID_ITEM_ID_FORMAT;
import static com.devxpress.auction.controller.AuctionController.ITEM_ID_MISMATCH;
import static com.devxpress.auction.controller.AuctionController.WINNING_BID_NOT_FOUND;
import static com.devxpress.auction.utils.TestUtils.getJacksonDateTimeConverter;
import static com.devxpress.auction.utils.TestUtils.mapToJson;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuctionControllerTest {

    private static final String BASE_ITEMS_URI = "/v1/items";

    @InjectMocks
    private AuctionController auctionController;

    @Mock
    private ItemService itemService;

    @Mock
    private BidService bidService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(auctionController)
                .setMessageConverters(getJacksonDateTimeConverter())
                .setControllerAdvice(new ControllerExceptionHandler())
                .build();
    }

    @Test
    public void getAllItems() throws Exception {

        when(itemService.getAllItems()).thenReturn(createTestItems());

        mockMvc.perform(get(BASE_ITEMS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Item 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].description", is("Item 2")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].description", is("Item 3")))
                .andExpect(jsonPath("$[3].id", is(4)))
                .andExpect(jsonPath("$[3].description", is("Item 4")))
                .andExpect(jsonPath("$[4].id", is(5)))
                .andExpect(jsonPath("$[4].description", is("Item 5")));

        verify(itemService).getAllItems();
    }

    @Test
    public void failGetAllItems() throws Exception {

        when(itemService.getAllItems()).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get(BASE_ITEMS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.code", is(UNEXPECTED_ERROR)))
                .andExpect(jsonPath("$.message", is(SYSTEM_ERROR_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]",
                        is("Failed to retrieve all auction items - Something went wrong")));

        verify(itemService).getAllItems();
    }

    @Test
    public void getItemsOnWhichUserHasBid() throws Exception {

        String userId = "bob";
        LocalDateTime now = LocalDateTime.now();

        List<BidDetail> bidDetails = new ArrayList<>();
        bidDetails.add(createTestBidDetail(2L, null, userId, new BigDecimal("20.00"), now));
        bidDetails.add(createTestBidDetail(4L, null, userId, new BigDecimal("10.00"), now));
        bidDetails.add(createTestBidDetail(2L, null, userId, new BigDecimal("25.00"), now));
        bidDetails.add(createTestBidDetail(3L, null, userId, new BigDecimal("30.00"), now));

        when(bidService.getBidsForUser(userId)).thenReturn(bidDetails);
        when(itemService.getItem(2L)).thenReturn(createTestItem(2L, "Description 2"));
        when(itemService.getItem(3L)).thenReturn(createTestItem(3L, "Description 3"));
        when(itemService.getItem(4L)).thenReturn(createTestItem(4L, "Description 4"));

        mockMvc.perform(get(BASE_ITEMS_URI + "?bid-user-id=" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].description", is("Description 2")))
                .andExpect(jsonPath("$[1].id", is(4)))
                .andExpect(jsonPath("$[1].description", is("Description 4")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].description", is("Description 3")));

        verify(bidService).getBidsForUser(userId);
        verify(itemService, times(4)).getItem(anyLong());
    }

    @Test
    public void failGetItemsOnWhichUserHasBid() throws Exception {

        String userId = "bob";

        when(bidService.getBidsForUser(userId)).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get(BASE_ITEMS_URI + "?bid-user-id=" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.code", is(UNEXPECTED_ERROR)))
                .andExpect(jsonPath("$.message", is(SYSTEM_ERROR_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]",
                        is("Failed to retrieve auction item(s) on which user : bob, has bid - Something went wrong")));

        verify(bidService).getBidsForUser(userId);
    }

    @Test
    public void getAllBidsForItem() throws Exception {

        long itemId = 3L;

        LocalDateTime now = LocalDateTime.now();

        List<BidDetail> bidDetails = new ArrayList<>();
        bidDetails.add(createTestBidDetail(itemId, null, "bob", new BigDecimal("20.25"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "alice", new BigDecimal("25.50"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "bob", new BigDecimal("30.75"), now));

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.getBidsForItem(itemId)).thenReturn(bidDetails);

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].itemId", is((int) itemId)))
                .andExpect(jsonPath("$[0].itemDescription", is("Description " + itemId)))
                .andExpect(jsonPath("$[0].userId", is("bob")))
                .andExpect(jsonPath("$[0].amount", is(20.25)))
                .andExpect(jsonPath("$[0].createdDateTime", startsWith(now.format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[1].itemId", is((int) itemId)))
                .andExpect(jsonPath("$[1].itemDescription", is("Description " + itemId)))
                .andExpect(jsonPath("$[1].userId", is("alice")))
                .andExpect(jsonPath("$[1].amount", is(25.5)))
                .andExpect(jsonPath("$[1].createdDateTime", startsWith(now.format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[2].itemId", is((int) itemId)))
                .andExpect(jsonPath("$[2].itemDescription", is("Description " + itemId)))
                .andExpect(jsonPath("$[2].userId", is("bob")))
                .andExpect(jsonPath("$[2].amount", is(30.75)))
                .andExpect(jsonPath("$[2].createdDateTime", startsWith(now.format(DateTimeFormatter.ISO_DATE_TIME))));

        verify(bidService).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void getAllBidsForItemWhenNoBidsExist() throws Exception {

        long itemId = 3L;

        List<BidDetail> bidDetails = new ArrayList<>();

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.getBidsForItem(itemId)).thenReturn(bidDetails);

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bidService).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void failGetAllBidsForItemNonIntegerItemId() throws Exception {

        String badItemId = "g8763t28dg8";

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids", badItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(MISSING_OR_INVALID_ARGUMENT_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is(String.format(INVALID_ITEM_ID_FORMAT, badItemId))));

        verify(bidService, never()).getBidsForItem(anyLong());
        verify(itemService, never()).getItem(anyLong());
    }

    @Test
    public void failGetAllBidsForItemNotFound() throws Exception {

        long itemId = 3L;

        when(itemService.getItem(itemId)).thenThrow(new ResourceNotFoundException("Something's missing"));

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(RESOURCE_NOT_FOUND_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is("Something's missing")));

        verify(bidService, never()).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void failGetAllBidsForItemUnexpectedError() throws Exception {

        long itemId = 3L;

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.getBidsForItem(itemId)).thenThrow(new RuntimeException("Something's wrong"));

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.code", is(UNEXPECTED_ERROR)))
                .andExpect(jsonPath("$.message", is(SYSTEM_ERROR_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]",
                        is("Failed to retrieve bid(s) on auction item with id : 3 - Something's wrong")));

        verify(bidService).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void getWinningBidForItem() throws Exception {

        long itemId = 3L;

        LocalDateTime now = LocalDateTime.now();

        List<BidDetail> bidDetails = new ArrayList<>();
        bidDetails.add(createTestBidDetail(itemId, null, "bob", new BigDecimal("20.25"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "alice", new BigDecimal("25.50"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "bob", new BigDecimal("30.75"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "alice", new BigDecimal("28.50"), now));

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.getBidsForItem(itemId)).thenReturn(bidDetails);

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids/winning", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is((int) itemId)))
                .andExpect(jsonPath("$.itemDescription", is("Description " + itemId)))
                .andExpect(jsonPath("$.userId", is("bob")))
                .andExpect(jsonPath("$.amount", is(30.75)))
                .andExpect(jsonPath("$.createdDateTime", startsWith(now.format(DateTimeFormatter.ISO_DATE_TIME))));

        verify(bidService).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void getWinningBidForItemWhenMultipleMaxBids() throws Exception {

        long itemId = 3L;

        LocalDateTime now = LocalDateTime.now();

        List<BidDetail> bidDetails = new ArrayList<>();
        bidDetails.add(createTestBidDetail(itemId, null, "bob", new BigDecimal("20.25"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "alice", new BigDecimal("25.50"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "bob", new BigDecimal("30.75"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "alice", new BigDecimal("28.50"), now));
        bidDetails.add(createTestBidDetail(itemId, null, "alice", new BigDecimal("30.75"), now));

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.getBidsForItem(itemId)).thenReturn(bidDetails);

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids/winning", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is((int) itemId)))
                .andExpect(jsonPath("$.itemDescription", is("Description " + itemId)))
                .andExpect(jsonPath("$.userId", is("bob")))
                .andExpect(jsonPath("$.amount", is(30.75)))
                .andExpect(jsonPath("$.createdDateTime", startsWith(now.format(DateTimeFormatter.ISO_DATE_TIME))));

        verify(bidService).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void getWinningBidForItemWhenNoBidsExist() throws Exception {

        long itemId = 3L;

        List<BidDetail> bidDetails = new ArrayList<>();

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.getBidsForItem(itemId)).thenReturn(bidDetails);

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids/winning", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(RESOURCE_NOT_FOUND_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is(String.format(WINNING_BID_NOT_FOUND, itemId))));

        verify(bidService).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void failGetWinningBidForItemNonIntegerItemId() throws Exception {

        String badItemId = "g8763t28dg8";

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids/winning", badItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(MISSING_OR_INVALID_ARGUMENT_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is(String.format(INVALID_ITEM_ID_FORMAT, badItemId))));

        verify(bidService, never()).getBidsForItem(anyLong());
        verify(itemService, never()).getItem(anyLong());
    }

    @Test
    public void failGetWinningBidForItemNotFound() throws Exception {

        long itemId = 3L;

        when(itemService.getItem(itemId)).thenThrow(new ResourceNotFoundException("Something's missing"));

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids/winning", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(RESOURCE_NOT_FOUND_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is("Something's missing")));

        verify(bidService, never()).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void failGetWinningBidForItemUnexpectedError() throws Exception {

        long itemId = 3L;

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.getBidsForItem(itemId)).thenThrow(new RuntimeException("Something's wrong"));

        mockMvc.perform(get(BASE_ITEMS_URI + "/{item-id}/bids/winning", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.code", is(UNEXPECTED_ERROR)))
                .andExpect(jsonPath("$.message", is(SYSTEM_ERROR_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]",
                        is("Failed to retrieve winning bid on auction item with id : 3 - Something's wrong")));

        verify(bidService).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }


    @Test
    public void createBid() throws Exception {

        long itemId = 999L;
        String userId = "bob";
        BigDecimal amount = new BigDecimal("35.58");

        Bid dto = createTestBid(itemId, userId, amount);

        LocalDateTime now = LocalDateTime.now();

        BidDetail added = createTestBidDetail(itemId, null, userId, amount, now);
        BidDetail returned = createTestBidDetail(itemId, "Description " + itemId, userId, amount, now);

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.createBid(any(Bid.class))).thenReturn(added);

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapToJson(returned)));

        ArgumentCaptor<Bid> dtoCaptor = ArgumentCaptor.forClass(Bid.class);
        verify(bidService).createBid(dtoCaptor.capture());
        verifyNoMoreInteractions(bidService);

        assertThat(dtoCaptor.getValue(), is(dto));
    }

    @Test
    public void failCreateBidNonIntegerItemId() throws Exception {

        long itemId = 999L;
        String userId = "bob";
        BigDecimal amount = new BigDecimal("35.58");

        Bid dto = createTestBid(itemId, userId, amount);

        String badItemId = "g8763t28dg8";

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", badItemId)
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(MISSING_OR_INVALID_ARGUMENT_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is(String.format(INVALID_ITEM_ID_FORMAT, badItemId))));

        verify(bidService, never()).getBidsForItem(anyLong());
        verify(itemService, never()).getItem(anyLong());
    }

    @Test
    public void failCreateBidItemIdMismatch() throws Exception {

        long itemId = 999L;
        String userId = "bob";
        BigDecimal amount = new BigDecimal("35.58");

        Bid dto = createTestBid(itemId, userId, amount);

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", "333")
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(MISSING_OR_INVALID_ARGUMENT_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is(ITEM_ID_MISMATCH)));

        verify(bidService, never()).getBidsForItem(anyLong());
        verify(itemService, never()).getItem(anyLong());
    }

    @Test
    public void failCreateBidItemNotFound() throws Exception {

        long itemId = 999L;
        String userId = "bob";
        BigDecimal amount = new BigDecimal("35.58");

        Bid dto = createTestBid(itemId, userId, amount);

        when(itemService.getItem(itemId)).thenThrow(new ResourceNotFoundException("Something's missing"));

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(RESOURCE_NOT_FOUND_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is("Something's missing")));

        verify(bidService, never()).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void failCreateBidInvalidResource() throws Exception {

        long itemId = 999L;

        Bid dto = createTestBid(itemId, null, null);

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.UNPROCESSABLE_ENTITY.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(INVALID_RESOURCE_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(2)))
                .andExpect(jsonPath("$.contextDetails[0]", is(EMPTY_USER_ID)))
                .andExpect(jsonPath("$.contextDetails[1]", is(EMPTY_AMOUNT)));

        verify(bidService, never()).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);

        // Reset the mocks so they can be reused
        reset(bidService);
        reset(itemService);

        dto = createTestBid(itemId, "", BigDecimal.ZERO);

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.UNPROCESSABLE_ENTITY.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(INVALID_RESOURCE_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(2)))
                .andExpect(jsonPath("$.contextDetails[0]", is(EMPTY_USER_ID)))
                .andExpect(jsonPath("$.contextDetails[1]", is(INVALID_AMOUNT)));

        verify(bidService, never()).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);

        // Reset the mocks so they can be reused
        reset(bidService);
        reset(itemService);

        dto = createTestBid(itemId, "bob", new BigDecimal("-27.45"));

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(HttpStatus.UNPROCESSABLE_ENTITY.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.UNPROCESSABLE_ENTITY.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(INVALID_RESOURCE_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is(INVALID_AMOUNT)));

        verify(bidService, never()).getBidsForItem(itemId);
        verify(itemService).getItem(itemId);
    }

    @Test
    public void failCreateBidUnexpectedError() throws Exception {

        long itemId = 999L;
        String userId = "bob";
        BigDecimal amount = new BigDecimal("35.58");

        Bid dto = createTestBid(itemId, userId, amount);

        when(itemService.getItem(itemId)).thenReturn(createTestItem(itemId, "Description " + itemId));
        when(bidService.createBid(any(Bid.class))).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(post(BASE_ITEMS_URI + "/{item-id}/bids", itemId)
                .content(mapToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(HttpStatus.CONFLICT.name())))
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.CONFLICT.value())))
                .andExpect(jsonPath("$.code", is(nullValue())))
                .andExpect(jsonPath("$.message", is(RESOURCE_OPERATION_ERROR_MSG)))
                .andExpect(jsonPath("$.isoDateTime", startsWith(LocalDate.now(ZoneOffset.UTC).toString() + "T")))
                .andExpect(jsonPath("$.isoDateTime", endsWith("Z")))
                .andExpect(jsonPath("$.contextDetails", hasSize(1)))
                .andExpect(jsonPath("$.contextDetails[0]", is(BID_CREATION_FAILED + "Something went wrong")));

        verify(bidService).createBid(any(Bid.class));
        verify(itemService).getItem(itemId);
    }

    private Item createTestItem(long itemId, String description) {
        return new Item(itemId, description);
    }

    private Set<Item> createTestItems() {

        Set<Item> items = new LinkedHashSet<>();

        items.add(createTestItem(1L, "Item 1"));
        items.add(createTestItem(2L, "Item 2"));
        items.add(createTestItem(3L, "Item 3"));
        items.add(createTestItem(4L, "Item 4"));
        items.add(createTestItem(5L, "Item 5"));

        return items;
    }

    private BidDetail createTestBidDetail(
            long itemId, String itemDescription, String userId, BigDecimal amount, LocalDateTime createdAt) {

        BidDetail bd = new BidDetail();
        bd.setItemId(itemId);
        bd.setItemDescription(itemDescription);
        bd.setUserId(userId);
        bd.setAmount(amount);
        bd.setCreatedDateTime(createdAt);

        return bd;
    }

    private Bid createTestBid(long itemId, String userId, BigDecimal amount) {

        Bid b = new Bid();
        b.setItemId(itemId);
        b.setUserId(userId);
        b.setAmount(amount);

        return b;
    }

}