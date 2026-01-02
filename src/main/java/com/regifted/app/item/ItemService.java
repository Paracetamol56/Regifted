package com.regifted.app.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordService;
import com.regifted.app.search.SearchRepository;

import com.regifted.app.user.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

@Service
public class ItemService {
  private final ItemRepository repository;
  private final KeywordService keywordService;
  private final SearchRepository searchRepository;

  public ItemService(ItemRepository repository, KeywordService keywordService, SearchRepository searchRepository) {
    this.repository = repository;
    this.keywordService = keywordService;
    this.searchRepository = searchRepository;
  }

  public Item createItem(ItemPostRequest req, User CurrentUser) {
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
    item.setUser(CurrentUser);

    Item res = repository.saveAndFlush(item);

     // Notifier les utilisateurs de la création d'un item correspondant à leurs recherches sauvegarder
    Set<User> users = this.getUserToNotify(res);

    if(users.isEmpty()){
      System.out.println("NoOne to notify");
      return res;
    }

    for (User user : users) {
      if(user.isNotification()){
        System.out.println("User with email : " + user.getEmail() + " has been notified ! ");
      }
    }
    return res;
  }


  private Set<User> getUserToNotify(Item item) {
    if (item == null || item.getUuid() == null) {
        return Collections.emptySet();
    }

    // Récupération des utilisateurs matchant les critères
    Set<User> usersToNotify = searchRepository.findUsersToNotify(item );
    return usersToNotify;
}


  public Page<Item> getItemSearchPage(
      int pageNumber,
      int pageSize,
      String query,
      String keyword,
      String sortBy,
      String direction) {
    Sort sort = buildSort(sortBy, direction);
    PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);

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

  public void deleteItemById(String id) {
    Item existing = repository.findById(id)
        .orElseThrow(() -> new NotFoundException(id));
    repository.delete(existing);
  }

  // Helper method to build Sort object
  private Sort buildSort(String sortBy, String direction) {
    // Default direction
    Sort.Direction dir = Sort.Direction.ASC; // default
    if (direction != null && !direction.isBlank()) {
      if (direction.equalsIgnoreCase("desc")) {
        dir = Sort.Direction.DESC;
      }
    }

    // Default sortBy
    if (sortBy == null || sortBy.isBlank()) {
      return Sort.by(dir, "title"); // default
    }

    switch (sortBy.toLowerCase()) {
      case "likes":
        return Sort.by(dir, "likes");
      case "createdat":
        return Sort.by(dir, "createdAt");
      case "title":
      default:
        return Sort.by(dir, "title");
    }
  }
}
