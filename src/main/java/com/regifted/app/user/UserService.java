package com.regifted.app.user;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.user.dto.UserPostRequest;

import com.regifted.app.item.ItemRepository;

import java.util.HashSet;

import org.springframework.security.crypto.password.PasswordEncoder;

@org.springframework.stereotype.Service
public class UserService {

  private final UserRepository userRepository;
  private final ItemRepository itemRepository;

  private PasswordEncoder PasswordEncoder;

  public UserService(UserRepository repo, ItemRepository itemRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = repo;
    this.itemRepository = itemRepository;
    this.PasswordEncoder = passwordEncoder;
  }

  public User createUser(UserPostRequest req) {

    User user = req.toUser();

    user.setPassword(this.PasswordEncoder.encode(req.getPassword()));

    return userRepository.save(user);
  }

  public User getByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  public User getByUuid(String uuid) {
    return userRepository.findById(uuid).orElse(null);
  }

  public Item addLike(User user, String itemUuid) {
    // Find the item or throw NotFoundException
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new NotFoundException(itemUuid));

    // Initialize likedItems if null
    if (user.getLikedItems() == null) {
      user.setLikedItems(new HashSet<>());
    }

    // Add like only if not already liked (idempotency)
    if (!user.getLikedItems().contains(item)) {
      user.getLikedItems().add(item);
      item.setLikes(item.getLikes() + 1);
      itemRepository.save(item); // Save the item with updated likes count
    }

    userRepository.save(user);
    return item;
  }

  public Item removeLike(User user, String itemUuid) {
    // Find the item or throw NotFoundException
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new NotFoundException(itemUuid));

    // Initialize likedItems if null to handle edge case
    if (user.getLikedItems() == null) {
      user.setLikedItems(new HashSet<>());
    }

    // Remove like only if currently liked (idempotency)
    if (user.getLikedItems().contains(item)) {
      user.getLikedItems().remove(item);
      // Prevent negative likes count
      item.setLikes(Math.max(0, item.getLikes() - 1));
      itemRepository.save(item); // Save the item with updated likes count
    }

    userRepository.save(user);
    return item;
  }

  public boolean hasLikedItem(User user, Item item) {
    return user.getLikedItems() != null && user.getLikedItems().contains(item);
  }

}
