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
import com.regifted.app.exception.CartStatusPermissionException;
import com.regifted.app.exception.InvalidStatusTransitionException;
import com.regifted.app.exception.NotFoundException;
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

  public Cart getCartByUuidForUser(String uuid, String email) {
    User user = userService.getByEmail(email);
    Cart cart = cartRepository.findByUuidAndUser(uuid, user);
    return cart;
  }

  @Transactional
  public Cart addItemToUserCarts(String itemUuid, String email) {
    User receiver = userService.getByEmail(email);
    Item item = itemRepository.findById(itemUuid)
        .orElseThrow(() -> new NotFoundException(itemUuid));

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
  public Cart removeItemFromUserCart(String itemUuid, String email) {
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
  public Cart changeStatus(String bundleUuid, CartStatus newStatus, String email) {
    Cart bundle = cartRepository.findById(bundleUuid)
        .orElseThrow(() -> new NotFoundException(bundleUuid));

    // Block transition to DRAFT
    if (newStatus == CartStatus.DRAFT) {
      throw InvalidStatusTransitionException.cannotSetToDraft();
    }

    // User can only change to SENT
    if (newStatus == CartStatus.SENT) {
      if (!bundle.getUser().getEmail().equals(email)) {
        throw CartStatusPermissionException.onlyUserCanSend();
      }
      // Validate current status (only DRAFT can be sent)
      if (bundle.getStatus() != CartStatus.DRAFT) {
        throw InvalidStatusTransitionException.onlyDraftCanBeSent();
      }
    }
    // Owner can only change to ACCEPTED or REFUSED
    else if (newStatus == CartStatus.ACCEPTED || newStatus == CartStatus.REFUSED) {
      if (!bundle.getOwner().getEmail().equals(email)) {
        throw CartStatusPermissionException.onlyOwnerCanAcceptOrRefuse();
      }
      // Validate current status (only SENT can be accepted/refused)
      if (bundle.getStatus() != CartStatus.SENT) {
        throw InvalidStatusTransitionException.onlySentCanBeAcceptedOrRefused();
      }
    }

    bundle.setStatus(newStatus);
    return cartRepository.save(bundle);
  }
}
