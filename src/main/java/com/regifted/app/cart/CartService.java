package com.regifted.app.cart;

import com.regifted.app.user.User;
import com.regifted.app.user.UserService;
import lombok.RequiredArgsConstructor;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.regifted.app.item.ItemRepository;
import com.regifted.app.item.Item;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final ItemRepository itemRepository;
  private final UserService userService;

  public Page<Cart> getCartsForUser(String email, Pageable pageable) {
    User user = userService.getByEmail(email);
    Page<Cart> carts = cartRepository.findAllByUser(user, pageable);
    return carts;
  }

  public Set<Cart> getAllCartsForUser(String email) {
    User user = userService.getByEmail(email);
    Set<Cart> carts = user.getCarts();
    return carts;
  }

  @Transactional
  public Cart addItemToUserBundles(String itemUuid, String email) {
    User receiver = userService.getByEmail(email);
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new RuntimeException("Item not found"));

    if (item.getUser().equals(receiver)) {
      throw new IllegalStateException("You cannot add your own item to your cart");
    }

    Cart currentBundle = receiver.getCarts().stream()
        .filter(b -> b.getOwner().getUuid().equals(item.getUser().getUuid()))
        .filter(b -> b.getStatus() == CartStatus.DRAFT)
        .findFirst()
        .orElseGet(() -> {
          Cart newBundle = new Cart();
          newBundle.setUser(receiver);
          newBundle.setOwner(item.getUser());
          newBundle.setStatus(CartStatus.DRAFT);
          Cart saved = cartRepository.save(newBundle);
          receiver.getCarts().add(saved);
          return saved;
        });

    if (!currentBundle.getItems().contains(item)) {
      currentBundle.getItems().add(item);
      item.getCarts().add(currentBundle);
    }

    itemRepository.save(item);
    return cartRepository.save(currentBundle);
  }

  @Transactional
  public Cart removeItemFromUserBundle(String itemUuid, String email) {
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new RuntimeException("Item not found"));

    Cart cart = item.getCarts().stream()
        .filter(b -> b.getUser().getEmail().equals(email))
        .filter(b -> b.getStatus() == CartStatus.DRAFT)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Item is not in your cart"));

    cart.getItems().remove(item);
    item.getCarts().remove(cart);
    itemRepository.save(item);

    if (cart.getItems().isEmpty()) {
      cart.getUser().getCarts().remove(cart);
      cartRepository.delete(cart);
      return null;
    }

    return cartRepository.save(cart);
  }

  @Transactional
  public void validateBundle(String bundleUuid, String email) {
    Cart bundle = cartRepository.findById(bundleUuid)
        .orElseThrow(() -> new RuntimeException("Lot non trouvé"));

    if (!bundle.getUser().getEmail().equals(email)) {
      throw new IllegalStateException("Ce lot ne vous appartient pas");
    }

    if (bundle.getItems().isEmpty()) {
      throw new IllegalStateException("Impossible de valider un lot vide");
    }

    bundle.setStatus(CartStatus.SENT);
    cartRepository.save(bundle);
  }

  @Transactional
  public void changeStatus(String bundleUuid, CartStatus newStatus, String email) {
    Cart bundle = cartRepository.findById(bundleUuid)
        .orElseThrow(() -> new RuntimeException("Lot introuvable"));

    if (newStatus == CartStatus.SENT) {
      if (!bundle.getUser().getEmail().equals(email))
        throw new AccessDeniedException("Action interdite");
    } else if (newStatus == CartStatus.ACCEPTED || newStatus == CartStatus.REFUSED) {
      if (!bundle.getOwner().getEmail().equals(email))
        throw new AccessDeniedException("Action interdite");
    }

    bundle.setStatus(newStatus);
    cartRepository.save(bundle);
  }
}
