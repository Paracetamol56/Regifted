package com.regifted.app.user;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.user.dto.UserPostRequest;

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

  public Item addLike(User user, String itemUuid) {
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new NotFoundException(itemUuid));

    // Initialize favorites if null
    if (user.getLikedItems() == null) {
      user.setLikedItems(new HashSet<>());
    }

    if (!user.getLikedItems().contains(item)) {
      user.getLikedItems().add(item);
    }
    repository.save(user);

    return item;
  }

  public Item removeLike(User user, String itemUuid) {
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new NotFoundException(itemUuid));

    if (user.getLikedItems() != null && user.getLikedItems().contains(item)) {
      user.getLikedItems().remove(item);
    }
    repository.save(user);

    return item;
  }

  public boolean hasLikedItem(User user, Item item) {
    return user.getLikedItems() != null && user.getLikedItems().contains(item);
  }

}
