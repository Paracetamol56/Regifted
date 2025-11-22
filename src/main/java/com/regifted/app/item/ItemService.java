package com.regifted.app.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordService;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ItemService {
  private final ItemRepository repository;
  private final KeywordService keywordService;

  public ItemService(ItemRepository repository, KeywordService keywordService) {
    this.repository = repository;
    this.keywordService = keywordService;
  }

  public Item createItem(ItemPostRequest req) {
    Item item = req.toItem();

    // Gestion des keywords
    Set<Keyword> keywords = new HashSet<>();
    if (req.getKeywords() != null && !req.getKeywords().isEmpty()) {
      for (String name : req.getKeywords()) {
        Keyword keyword = keywordService.getOrCreateKeyword(name);
        if (keyword != null) {
          keywords.add(keyword);
        }
      }
    }
    item.setKeywords(keywords);

    return repository.save(item);
  }

  public Page<Item> getItemSearchPage(int pageNumber, int pageSize, String query, String keyword) {
    PageRequest pageable = PageRequest.of(pageNumber, pageSize);
    if (query != null && !query.trim().isEmpty()) {
      return repository.searchByTitleOrDescription(query, pageable);
    } else if (keyword != null && !keyword.trim().isEmpty()) {
      Keyword kw = keywordService.getByName(keyword.trim().toLowerCase());
      if (kw == null) {
        return Page.empty(pageable);
      }
      return repository.findByKeywordsContaining(kw, pageable);
    }
    return repository.findAll(pageable);
  }

  public List<Item> getAllItems() {
    return repository.findAll();
  }

  public Item getById(String id) {
    return repository.findById(id)
        .orElseThrow(() -> new NotFoundException(id));
  }

  public Item updateItemById(String id, ItemPutRequest req) {
    Item existing = repository.findById(id)
        .orElseThrow(() -> new NotFoundException(id));

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
        Keyword keyword = keywordService.getOrCreateKeyword(name);
        if (keyword != null) {
          keywords.add(keyword);
        }
      }
      existing.setKeywords(keywords);
    }

    existing.setUpdatedAt(Instant.now());
    return repository.save(existing);
  }
}
