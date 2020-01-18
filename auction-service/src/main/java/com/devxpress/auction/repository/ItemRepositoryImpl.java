package com.devxpress.auction.repository;

import com.devxpress.auction.entity.ItemEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

@Component
public class ItemRepositoryImpl implements ItemRepository {

    private static final Map<Long, ItemEntity> ITEM_MAP_REPO = new LinkedHashMap<>();

    static {
        ITEM_MAP_REPO.put(1L, new ItemEntity(1L, "Awesome item 1", "Books"));
        ITEM_MAP_REPO.put(2L, new ItemEntity(2L, "Extraordinary item 2", "Electronics"));
        ITEM_MAP_REPO.put(3L, new ItemEntity(3L, "Fabulous item 3", "Jewelry"));
        ITEM_MAP_REPO.put(4L, new ItemEntity(4L, "Magnificent item 4", "Travel"));
        ITEM_MAP_REPO.put(5L, new ItemEntity(5L, "Quite remarkable item 5", "Toys"));
    }

    @Override
    public Iterable<ItemEntity> findAll() {
        return new LinkedHashSet<>(ITEM_MAP_REPO.values());
    }

    @Override
    public Optional<ItemEntity> findById(Long itemId) {
        return Optional.ofNullable(ITEM_MAP_REPO.get(itemId));
    }
}
