package com.regifted.app.user;

import com.regifted.app.item.Item;
import com.regifted.app.user.dto.UserPostRequest;

import jakarta.persistence.EntityNotFoundException;

import com.regifted.app.item.ItemRepository;

import java.util.HashSet;

@org.springframework.stereotype.Service
public class UserService {

  private final UserRepository repository;
  private final ItemRepository itemRepository;

  public UserService(UserRepository repo, ItemRepository itemRepository) {
    this.repository = repo;
    this.itemRepository = itemRepository;
  }

  public User createUser(UserPostRequest req) {
    return repository.save(req.toUser());
  }

  public User getByEmail(String email) {
    return repository.findByEmail(email).orElse(null);
  }

  public User getByUuid(String uuid) {
    return repository.findById(uuid).orElse(null);
  }

  public void addLike(String userUuid, String itemUuid) {
    User user = repository.findById(userUuid)
        .orElseThrow(() -> new EntityNotFoundException("User not found: " + userUuid));

    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemUuid));

    // Initialize favorites if null
    if (user.getFavoriteItems() == null) {
      user.setFavoriteItems(new HashSet<>());
    }

    if (!user.getFavoriteItems().contains(item)) {
      user.getFavoriteItems().add(item);
    }

    repository.save(user);
  }

  public void removeLike(String userUuid, String itemUuid) {
    User user = repository.findById(userUuid)
        .orElseThrow(() -> new EntityNotFoundException("User not found: " + userUuid));

    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemUuid));

    if (user.getFavoriteItems() != null && user.getFavoriteItems().contains(item)) {
      user.getFavoriteItems().remove(item);
    }

    repository.save(user);
  }

}
