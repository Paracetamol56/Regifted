package com.regifted.app.item;

import org.springframework.stereotype.Service;

import com.regifted.app.item.dto.ItemPostRequest;

import java.time.Instant;
import java.util.List;

@Service
public class ItemService {

  private final ItemRepository repository;
  private final KeywordRepository keywordRepository;

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

   public Item updateItembyId(String id, ItemPostRequest itemRequest) {
    Item existing = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found"));

    if (itemRequest.getTitle() != null) {
      existing.setTitle(itemRequest.getTitle());
    }
    existing.setDescription(itemRequest.getDescription());
    if (itemRequest.getLatitude() != null) {
      existing.setLatitude(itemRequest.getLatitude());
    }
    if (itemRequest.getLongitude() != null) {
      existing.setLongitude(itemRequest.getLongitude());
    }
    if (itemRequest.getState() != null) {
      existing.setState(itemRequest.getState());
    }

    existing.setUpdatedAt(Instant.now());

  System.out.println("Updated Item: " + existing);

    return repository.save(existing);
  }
}
