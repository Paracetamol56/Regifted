package com.regifted.app.user;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.user.dto.UserPostRequest;

import com.regifted.app.item.ItemRepository;

import java.util.HashSet;

import org.springframework.security.crypto.password.PasswordEncoder;

@org.springframework.stereotype.Service
public class UserService {

  private final UserRepository repository;
  private final ItemRepository itemRepository;

  private PasswordEncoder PasswordEncoder;

  public UserService(UserRepository repo, ItemRepository itemRepository , PasswordEncoder passwordEncoder) {
    this.repository = repo;
    this.itemRepository = itemRepository;
    this.PasswordEncoder = passwordEncoder;
  }

  public User createUser(UserPostRequest req) {

    User user = req.toUser();

    user.setPassword(this.PasswordEncoder.encode(req.getPassword()));

    return repository.save(user);
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
    item.setLikes(item.getLikes() + 1);

    return item;
  }

  public Item removeLike(User user, String itemUuid) {
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new NotFoundException(itemUuid));

    if (user.getLikedItems() != null && user.getLikedItems().contains(item)) {
      user.getLikedItems().remove(item);
    }
    repository.save(user);
    item.setLikes(item.getLikes() - 1);

    return item;
  }

  public boolean hasLikedItem(User user, Item item) {
    return user.getLikedItems() != null && user.getLikedItems().contains(item);
  }

}
