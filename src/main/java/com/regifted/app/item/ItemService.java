package com.regifted.app.item;

import org.springframework.stereotype.Service;

import com.regifted.app.item.dto.ItemPostRequest;

import java.util.List;

@Service
public class ItemService {

  private final ItemRepository repository;

  public ItemService(ItemRepository repository) {
    this.repository = repository;
  }

  public Item createItem(ItemPostRequest itemRequest) {
    return repository.save(itemRequest.toItem());
  }

  public List<Item> getAllItems() {
    return repository.findAll();
  }

  public Item getById(String id) {
    return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found"));
  }
}
