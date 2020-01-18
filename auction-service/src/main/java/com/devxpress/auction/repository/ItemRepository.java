package com.devxpress.auction.repository;

import com.devxpress.auction.entity.ItemEntity;

import java.util.Optional;

public interface ItemRepository {

    Iterable<ItemEntity> findAll();

    Optional<ItemEntity> findById(Long itemId);

}
