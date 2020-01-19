package com.devxpress.auction.controller;

import com.devxpress.auction.api.v1.model.BidDetail;
import com.devxpress.auction.api.v1.model.Item;
import com.devxpress.auction.service.BidService;
import com.devxpress.auction.service.ItemService;
import org.junit.Before;
import org.junit.Test;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.devxpress.auction.api.ApiErrorCode.UNEXPECTED_ERROR;
import static com.devxpress.auction.api.ApiErrorMessage.SYSTEM_ERROR_MSG;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        bidDetails.add(createTestBidDetail(2L, "Description 2", userId, new BigDecimal("20.00"), now));
        bidDetails.add(createTestBidDetail(4L, "Description 4", userId, new BigDecimal("10.00"), now));
        bidDetails.add(createTestBidDetail(2L, "Description 2", userId, new BigDecimal("25.00"), now));
        bidDetails.add(createTestBidDetail(3L, "Description 3", userId, new BigDecimal("30.00"), now));

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
    public void getAllBidsForItem() {
        // TODO
    }

    @Test
    public void getWinningBidForItem() {
        // TODO
    }

    @Test
    public void createBid() {
        // TODO
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
            long id, String description, String user, BigDecimal amount, LocalDateTime createdAt) {

        BidDetail bd = new BidDetail();
        bd.setItemId(id);
        bd.setItemDescription(description);
        bd.setUserId(user);
        bd.setAmount(amount);
        bd.setCreatedDateTime(createdAt);

        return bd;
    }

}