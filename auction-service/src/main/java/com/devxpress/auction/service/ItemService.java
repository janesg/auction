package com.devxpress.auction.service;

import com.devxpress.auction.api.v1.model.Item;

import java.util.Set;

public interface ItemService {

    Set<Item> getAllItems();

    Item getItem(Long itemId);

}
