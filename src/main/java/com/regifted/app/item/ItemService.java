package com.regifted.app.item;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemService {

  private final ItemRepository repository;

  public ItemService(ItemRepository repository) {
    this.repository = repository;
  }

  public List<Item> getAllItems() {
    return repository.findAll();
  }

  public Item getById(String id) {
    return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found"));
  }
}
