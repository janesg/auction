package com.devxpress.auction.service;

import com.devxpress.auction.api.exception.ResourceNotFoundException;
import com.devxpress.auction.api.v1.mapper.ItemMapper;
import com.devxpress.auction.api.v1.model.Item;
import com.devxpress.auction.entity.ItemEntity;
import com.devxpress.auction.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    static final String ITEM_NOT_EXIST_MSG = "Item does not exist for identifier : %s";

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public Set<Item> getAllItems() {
        return StreamSupport.stream(itemRepository.findAll().spliterator(), false)
                .map(itemMapper::itemEntityToItem)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Item getItem(Long itemId) {
        Optional<ItemEntity> iOpt = itemRepository.findById(itemId);

        return iOpt.map(itemMapper::itemEntityToItem)
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(ITEM_NOT_EXIST_MSG, itemId)));
    }

}
