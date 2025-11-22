package com.regifted.app.user;

import com.regifted.app.item.Item;
import com.regifted.app.user.dto.UserPostRequest;
import com.regifted.app.item.ItemRepository;

import java.util.Set;

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

  public boolean toggleFavorite(String uuidItem, String uuidUser) {
    // Récupération de l'utilisateur
    User user = repository.findById(uuidUser).orElse(null);
    if (user == null) {
      System.out.println("User not found: " + uuidUser);
      return false;
    }

    // Récupération de l'item
    Item item = itemRepository.findById(uuidItem).orElse(null);
    if (item == null) {
      System.out.println("Item not found: " + uuidItem);
      return false;
    }

    Set<Item> favorites = user.getFavoriteItems();
    boolean isNowFavorite;

    if (favorites.contains(item)) {
      favorites.remove(item);
      isNowFavorite = false;
    } else {
      favorites.add(item);
      isNowFavorite = true;
    }

    repository.save(user);

    System.out.println("Toggle favorite for user " + uuidUser + " and item " + uuidItem
        + " -> now favorite: " + isNowFavorite);
    return isNowFavorite;
  }

}
