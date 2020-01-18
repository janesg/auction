package com.devxpress.auction.controller;

import com.devxpress.auction.api.v1.model.Item;
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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.devxpress.auction.api.ApiErrorCode.UNEXPECTED_ERROR;
import static com.devxpress.auction.api.ApiErrorMessage.SYSTEM_ERROR_MSG;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ItemControllerTest {

    private static final String BASE_ITEMS_URI = "/v1/items";

    @InjectMocks
    private ItemController itemController;

    @Mock
    private ItemService itemService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
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

    private Set<Item> createTestItems() {

        Set<Item> items = new LinkedHashSet<>();

        items.add(new Item(1L, "Item 1"));
        items.add(new Item(2L, "Item 2"));
        items.add(new Item(3L, "Item 3"));
        items.add(new Item(4L, "Item 4"));
        items.add(new Item(5L, "Item 5"));

        return items;
    }

}