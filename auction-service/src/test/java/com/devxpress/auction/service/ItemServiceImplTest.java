package com.devxpress.auction.service;

import com.devxpress.auction.api.v1.mapper.ItemMapper;
import com.devxpress.auction.api.v1.mapper.ItemMapperImpl;
import com.devxpress.auction.api.v1.model.Item;
import com.devxpress.auction.entity.ItemEntity;
import com.devxpress.auction.repository.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemServiceImplTest {

    // Class under test
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    private ItemMapper itemMapper = new ItemMapperImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        itemService = new ItemServiceImpl(itemRepository, itemMapper);
    }

    @Test
    public void getAllItems() {

        when(itemRepository.findAll()).thenReturn(createTestItems());

        Set<Item> items = itemService.getAllItems();

        assertThat(items.size(), is(5));

        long i = 1;

        for (Item item : items) {
            assertThat(item.getId(), is(i));
            assertThat(item.getDescription(), is("Item " + i));
            i++;
        }

        verify(itemRepository).findAll();
    }

    private Set<ItemEntity> createTestItems() {

        Set<ItemEntity> items = new LinkedHashSet<>();

        items.add(new ItemEntity(1L, "Item 1", "Category A"));
        items.add(new ItemEntity(2L, "Item 2", "Category B"));
        items.add(new ItemEntity(3L, "Item 3", "Category C"));
        items.add(new ItemEntity(4L, "Item 4", "Category D"));
        items.add(new ItemEntity(5L, "Item 5", "Category E"));

        return items;
    }

}