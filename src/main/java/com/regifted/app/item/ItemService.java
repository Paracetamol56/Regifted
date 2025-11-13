package com.regifted.app.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.item.exception.ItemNotFoundException;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
    item.setUuid(UUID.randomUUID().toString());
    item.setTitle(req.getTitle());
    item.setDescription(req.getDescription());
    item.setLatitude(req.getLatitude());
    item.setLongitude(req.getLongitude());
    item.setState(req.getState());
    Instant now = Instant.now();
    item.setCreatedAt(now);
    item.setUpdatedAt(now);

    // Gestion des keywords
    Set<Keyword> keywords = new HashSet<>();
    if (req.getKeywords() != null && !req.getKeywords().isEmpty()) {
      for (String name : req.getKeywords()) {
        Keyword keyword = getOrCreateKeyword(name);
        if (keyword != null) {
          keywords.add(keyword);
        }
      }
    }
    item.setKeyword(keywords);

    return repository.save(item);
  }

  private Keyword getOrCreateKeyword(String name) {
    if (name == null || name.trim().isEmpty()) {
      return null;
    }

    String cleanName = name.trim().toLowerCase();

    // Cherche d'abord si le keyword existe
    Optional<Keyword> existing = keywordRepository.findByName(cleanName);
    if (existing.isPresent()) {
      return existing.get();
    }

    // Tente de créer le keyword
    try {
      Keyword keyword = new Keyword();
      keyword.setName(cleanName);
      return keywordRepository.save(keyword);
    } catch (Exception e) {
      // Si échec (contrainte UNIQUE), récupère celui qui existe
      Optional<Keyword> retry = keywordRepository.findByName(cleanName);
      if (retry.isPresent()) {
        return retry.get();
      }
      System.err.println("Erreur création keyword '" + cleanName + "': " + e.getMessage());
      return null;
    }
  }

  public Page<Item> getItemSearchPage(int pageNumber, int pageSize, String query) {
    PageRequest pageable = PageRequest.of(pageNumber, pageSize);
    if (query == null || query.trim().isEmpty()) {
      return repository.findAll(pageable);
    } else {
      return repository.searchByTitleOrDescription(query, pageable);
    }
  }

  public List<Item> getAllItems() {
    return repository.findAll();
  }

  public Item getById(String id) {
    return repository.findById(id)
        .orElseThrow(() -> new ItemNotFoundException(id));
  }

  public Item updateItemById(String id, ItemPutRequest req) {
    Item existing = repository.findById(id)
        .orElseThrow(() -> new ItemNotFoundException(id));

    if (req.getTitle() != null) {
      existing.setTitle(req.getTitle());
    }
    if (req.getDescription() != null) {
      existing.setDescription(req.getDescription());
    }
    if (req.getLatitude() != null) {
      existing.setLatitude(req.getLatitude());
    }
    if (req.getLongitude() != null) {
      existing.setLongitude(req.getLongitude());
    }
    if (req.getState() != null) {
      existing.setState(req.getState());
    }

    // Gestion des keywords lors de l'update
    if (req.getKeywords() != null) {
      Set<Keyword> keywords = new HashSet<>();
      for (String name : req.getKeywords()) {
        Keyword keyword = getOrCreateKeyword(name);
        if (keyword != null) {
          keywords.add(keyword);
        }
      }
      existing.setKeyword(keywords);
    }

    existing.setUpdatedAt(Instant.now());
    return repository.save(existing);
  }
}
