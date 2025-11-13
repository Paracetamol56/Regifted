package com.regifted.app.item;

import org.springframework.stereotype.Service;

import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class ItemService {

  private final ItemRepository repository;
  private final KeywordRepository keywordRepository;

  public ItemService(ItemRepository repository, KeywordRepository keywordRepository) {
    this.repository = repository;
    this.keywordRepository = keywordRepository;
  }

  public Item createItem(ItemPostRequest req) {
     Item item = new Item();
        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setLatitude(req.getLatitude());
        item.setLongitude(req.getLongitude());
        item.setState(req.getState());

        /*if (req.getUserId() != null) {
            userRepository.findById(req.getUserId()).ifPresent(item::setUser);
        }*/

        // Entités Keyword
       Set<Keyword> keywordEntities = req.getKeywords().stream()
        .map(k -> keywordRepository.findByName(k)
            .orElseGet(() -> {
                Keyword newKeyword = new Keyword();
                newKeyword.setName(k);
                return keywordRepository.save(newKeyword);
            })
        )
        .collect(Collectors.toSet());

        item.setKeyword(keywordEntities);
        return this.repository.save(item);
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
