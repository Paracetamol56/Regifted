package com.regifted.app.cart;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.exception.CartStatusPermissionException;
import com.regifted.app.exception.InvalidStatusTransitionException;
import com.regifted.app.item.Item;
import com.regifted.app.item.ItemRepository;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

  @Mock
  private CartRepository cartRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private CartService cartService;

  private User testUser;
  private User itemOwner;
  private User otherUser;
  private Item testItem;
  private Cart testCart;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setUuid("user-123");
    testUser.setEmail("test@example.com");
    testUser.setCarts(new HashSet<>());

    itemOwner = new User();
    itemOwner.setUuid("owner-456");
    itemOwner.setEmail("owner@example.com");
    itemOwner.setCarts(new HashSet<>());

    otherUser = new User();
    otherUser.setUuid("other-789");
    otherUser.setEmail("other@example.com");
    otherUser.setCarts(new HashSet<>());

    testItem = new Item();
    testItem.setUuid("item-123");
    testItem.setTitle("Vintage Camera");
    testItem.setUser(itemOwner);
    testItem.setCarts(new HashSet<>());

    testCart = new Cart();
    testCart.setUuid("cart-123");
    testCart.setUser(testUser);
    testCart.setOwner(itemOwner);
    testCart.setStatus(CartStatus.DRAFT);
    testCart.setItems(new HashSet<>());
  }

  private Cart createCart(String uuid, User user, User owner, CartStatus status) {
    Cart cart = new Cart();
    cart.setUuid(uuid);
    cart.setUser(user);
    cart.setOwner(owner);
    cart.setStatus(status);
    cart.setItems(new HashSet<>());
    return cart;
  }

  // ========== getCartsForUser Tests ==========

  @Test
  @DisplayName("Should return page of carts for user")
  void testGetCartsForUser_Success() {
    Cart cart1 = createCart("cart-1", testUser, itemOwner, CartStatus.DRAFT);
    Cart cart2 = createCart("cart-2", testUser, otherUser, CartStatus.SENT);
    List<Cart> carts = Arrays.asList(cart1, cart2);
    Page<Cart> cartPage = new PageImpl<>(carts);
    Pageable pageable = PageRequest.of(0, 10);

    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(cartRepository.findAllByUser(testUser, pageable)).thenReturn(cartPage);

    Page<Cart> result = cartService.getCartsForUser(testUser.getEmail(), pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).containsExactly(cart1, cart2);
    verify(userService, times(1)).getByEmail(testUser.getEmail());
    verify(cartRepository, times(1)).findAllByUser(testUser, pageable);
  }

  @Test
  @DisplayName("Should return empty page when user has no carts")
  void testGetCartsForUser_EmptyResult() {
    Page<Cart> emptyPage = Page.empty();
    Pageable pageable = PageRequest.of(0, 10);

    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(cartRepository.findAllByUser(testUser, pageable)).thenReturn(emptyPage);

    Page<Cart> result = cartService.getCartsForUser(testUser.getEmail(), pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  @Test
  @DisplayName("Should call repository with correct parameters")
  void testGetCartsForUser_CallsRepository() {
    Pageable pageable = PageRequest.of(0, 10);
    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(cartRepository.findAllByUser(testUser, pageable)).thenReturn(Page.empty());

    cartService.getCartsForUser(testUser.getEmail(), pageable);

    verify(userService, times(1)).getByEmail(testUser.getEmail());
    verify(cartRepository, times(1)).findAllByUser(testUser, pageable);
  }

  // ========== getAllCartsForUser Tests ==========

  @Test
  @DisplayName("Should return all carts for user")
  void testGetAllCartsForUser_Success() {
    Cart cart1 = createCart("cart-1", testUser, itemOwner, CartStatus.DRAFT);
    Cart cart2 = createCart("cart-2", testUser, otherUser, CartStatus.SENT);
    Set<Cart> carts = new HashSet<>(Arrays.asList(cart1, cart2));
    testUser.setCarts(carts);

    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);

    Set<Cart> result = cartService.getAllCartsForUser(testUser.getEmail());

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrder(cart1, cart2);
    verify(userService, times(1)).getByEmail(testUser.getEmail());
  }

  @Test
  @DisplayName("Should return empty set when user has no carts")
  void testGetAllCartsForUser_EmptyResult() {
    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);

    Set<Cart> result = cartService.getAllCartsForUser(testUser.getEmail());

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  // ========== getCartByUuidForUser Tests ==========

  @Test
  @DisplayName("Should return cart by UUID for user")
  void testGetCartByUuidForUser_Success() {
    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(cartRepository.findByUuidAndUser(testCart.getUuid(), testUser)).thenReturn(testCart);

    Cart result = cartService.getCartByUuidForUser(testCart.getUuid(), testUser.getEmail());

    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isEqualTo(testCart.getUuid());
    assertThat(result.getUser()).isEqualTo(testUser);
    verify(userService, times(1)).getByEmail(testUser.getEmail());
    verify(cartRepository, times(1)).findByUuidAndUser(testCart.getUuid(), testUser);
  }

  @Test
  @DisplayName("Should return null when cart not found for user")
  void testGetCartByUuidForUser_NotFound() {
    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(cartRepository.findByUuidAndUser("non-existent", testUser)).thenReturn(null);

    Cart result = cartService.getCartByUuidForUser("non-existent", testUser.getEmail());

    assertThat(result).isNull();
  }

  // ========== addItemToUserCarts Tests ==========

  @Test
  @DisplayName("Should create new cart when adding first item from owner")
  void testAddItemToUserCarts_CreatesNewCart() {
    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));
    when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
      Cart cart = invocation.getArgument(0);
      cart.setUuid("new-cart-uuid");
      return cart;
    });
    when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Cart result = cartService.addItemToUserCarts(testItem.getUuid(), testUser.getEmail());

    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(testUser);
    assertThat(result.getOwner()).isEqualTo(itemOwner);
    assertThat(result.getStatus()).isEqualTo(CartStatus.DRAFT);
    assertThat(result.getItems()).contains(testItem);
    verify(cartRepository, times(2)).save(any(Cart.class)); // Once for new cart, once after adding item
    verify(itemRepository, times(1)).save(testItem);
  }

  @Test
  @DisplayName("Should add item to existing draft cart from same owner")
  void testAddItemToUserCarts_AddsToExistingCart() {
    testUser.getCarts().add(testCart);

    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
    when(itemRepository.save(any(Item.class))).thenReturn(testItem);

    Cart result = cartService.addItemToUserCarts(testItem.getUuid(), testUser.getEmail());

    assertThat(result).isNotNull();
    assertThat(result.getItems()).contains(testItem);
    assertThat(testItem.getCarts()).contains(testCart);
    verify(cartRepository, times(1)).save(testCart);
    verify(itemRepository, times(1)).save(testItem);
  }

  @Test
  @DisplayName("Should not add duplicate item to cart")
  void testAddItemToUserCarts_PreventsDuplicates() {
    testCart.getItems().add(testItem);
    testItem.getCarts().add(testCart);
    testUser.getCarts().add(testCart);

    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
    when(itemRepository.save(any(Item.class))).thenReturn(testItem);

    Cart result = cartService.addItemToUserCarts(testItem.getUuid(), testUser.getEmail());

    assertThat(result.getItems()).hasSize(1);
    assertThat(result.getItems()).contains(testItem);
  }

  @Test
  @DisplayName("Should throw NotFoundException when item doesn't exist")
  void testAddItemToUserCarts_ItemNotFound() {
    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(itemRepository.findById("non-existent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.addItemToUserCarts("non-existent", testUser.getEmail()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("non-existent");

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw IllegalStateException when adding own item")
  void testAddItemToUserCarts_CannotAddOwnItem() {
    testItem.setUser(testUser);

    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));

    assertThatThrownBy(() -> cartService.addItemToUserCarts(testItem.getUuid(), testUser.getEmail()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cannot add your own item");

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should not add item to non-draft cart")
  void testAddItemToUserCarts_IgnoresNonDraftCarts() {
    Cart sentCart = createCart("sent-cart", testUser, itemOwner, CartStatus.SENT);
    testUser.getCarts().add(sentCart);

    when(userService.getByEmail(testUser.getEmail())).thenReturn(testUser);
    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));
    when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
      Cart cart = invocation.getArgument(0);
      if (cart.getUuid() == null) {
        cart.setUuid("new-draft-cart");
      }
      return cart;
    });
    when(itemRepository.save(any(Item.class))).thenReturn(testItem);

    Cart result = cartService.addItemToUserCarts(testItem.getUuid(), testUser.getEmail());

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(CartStatus.DRAFT);
    assertThat(result).isNotEqualTo(sentCart);
    assertThat(sentCart.getItems()).isEmpty();
  }

  // ========== removeItemFromUserCart Tests ==========

  @Test
  @DisplayName("Should remove item from cart")
  void testRemoveItemFromUserCart_Success() {
    testCart.getItems().add(testItem);
    testItem.getCarts().add(testCart);

    Item anotherItem = new Item();
    anotherItem.setUuid("another-item");
    testCart.getItems().add(anotherItem);

    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));
    when(itemRepository.save(any(Item.class))).thenReturn(testItem);
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    Cart result = cartService.removeItemFromUserCart(testItem.getUuid(), testUser.getEmail());

    assertThat(result).isNotNull();
    assertThat(result.getItems()).doesNotContain(testItem);
    assertThat(testItem.getCarts()).doesNotContain(testCart);
    verify(itemRepository, times(1)).save(testItem);
    verify(cartRepository, times(1)).save(testCart);
  }

  @Test
  @DisplayName("Should delete cart when removing last item")
  void testRemoveItemFromUserCart_DeletesEmptyCart() {
    testCart.getItems().add(testItem);
    testItem.getCarts().add(testCart);
    testUser.getCarts().add(testCart);

    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));
    when(itemRepository.save(any(Item.class))).thenReturn(testItem);
    doNothing().when(cartRepository).delete(testCart);

    Cart result = cartService.removeItemFromUserCart(testItem.getUuid(), testUser.getEmail());

    assertThat(result).isNull();
    assertThat(testCart.getItems()).isEmpty();
    assertThat(testUser.getCarts()).doesNotContain(testCart);
    verify(cartRepository, times(1)).delete(testCart);
    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when item not found")
  void testRemoveItemFromUserCart_ItemNotFound() {
    when(itemRepository.findById("non-existent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.removeItemFromUserCart("non-existent", testUser.getEmail()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Item not found");

    verify(cartRepository, never()).save(any());
    verify(cartRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should throw exception when item not in user cart")
  void testRemoveItemFromUserCart_ItemNotInCart() {
    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));

    assertThatThrownBy(() -> cartService.removeItemFromUserCart(testItem.getUuid(), testUser.getEmail()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("not in your cart");

    verify(cartRepository, never()).save(any());
    verify(cartRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should only remove from draft cart")
  void testRemoveItemFromUserCart_OnlyFromDraftCart() {
    Cart sentCart = createCart("sent-cart", testUser, itemOwner, CartStatus.SENT);
    sentCart.getItems().add(testItem);
    testItem.getCarts().add(sentCart);

    when(itemRepository.findById(testItem.getUuid())).thenReturn(Optional.of(testItem));

    assertThatThrownBy(() -> cartService.removeItemFromUserCart(testItem.getUuid(), testUser.getEmail()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("not in your cart");
  }

  // ========== changeStatus Tests ==========

  @Test
  @DisplayName("Should change status from DRAFT to SENT by user")
  void testChangeStatus_DraftToSent_Success() {
    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    Cart result = cartService.changeStatus(testCart.getUuid(), CartStatus.SENT, testUser.getEmail());

    assertThat(result.getStatus()).isEqualTo(CartStatus.SENT);
    verify(cartRepository, times(1)).save(testCart);
  }

  @Test
  @DisplayName("Should change status from SENT to ACCEPTED by owner")
  void testChangeStatus_SentToAccepted_Success() {
    testCart.setStatus(CartStatus.SENT);

    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    Cart result = cartService.changeStatus(testCart.getUuid(), CartStatus.ACCEPTED, itemOwner.getEmail());

    assertThat(result.getStatus()).isEqualTo(CartStatus.ACCEPTED);
    verify(cartRepository, times(1)).save(testCart);
  }

  @Test
  @DisplayName("Should change status from SENT to REFUSED by owner")
  void testChangeStatus_SentToRefused_Success() {
    testCart.setStatus(CartStatus.SENT);

    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    Cart result = cartService.changeStatus(testCart.getUuid(), CartStatus.REFUSED, itemOwner.getEmail());

    assertThat(result.getStatus()).isEqualTo(CartStatus.REFUSED);
    verify(cartRepository, times(1)).save(testCart);
  }

  @Test
  @DisplayName("Should throw exception when cart not found")
  void testChangeStatus_CartNotFound() {
    when(cartRepository.findById("non-existent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.changeStatus("non-existent", CartStatus.SENT, testUser.getEmail()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("non-existent");

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when changing to DRAFT")
  void testChangeStatus_CannotChangeToDraft() {
    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));

    assertThatThrownBy(() -> cartService.changeStatus(testCart.getUuid(), CartStatus.DRAFT, testUser.getEmail()))
        .isInstanceOf(InvalidStatusTransitionException.class);

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when non-user tries to send")
  void testChangeStatus_OnlyUserCanSend() {
    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));

    assertThatThrownBy(() -> cartService.changeStatus(testCart.getUuid(), CartStatus.SENT, itemOwner.getEmail()))
        .isInstanceOf(CartStatusPermissionException.class);

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when sending non-draft cart")
  void testChangeStatus_OnlyDraftCanBeSent() {
    testCart.setStatus(CartStatus.SENT);

    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));

    assertThatThrownBy(() -> cartService.changeStatus(testCart.getUuid(), CartStatus.SENT, testUser.getEmail()))
        .isInstanceOf(InvalidStatusTransitionException.class);

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when non-owner tries to accept")
  void testChangeStatus_OnlyOwnerCanAccept() {
    testCart.setStatus(CartStatus.SENT);

    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));

    assertThatThrownBy(() -> cartService.changeStatus(testCart.getUuid(), CartStatus.ACCEPTED, testUser.getEmail()))
        .isInstanceOf(CartStatusPermissionException.class);

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when non-owner tries to refuse")
  void testChangeStatus_OnlyOwnerCanRefuse() {
    testCart.setStatus(CartStatus.SENT);

    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));

    assertThatThrownBy(() -> cartService.changeStatus(testCart.getUuid(), CartStatus.REFUSED, testUser.getEmail()))
        .isInstanceOf(CartStatusPermissionException.class);

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when accepting non-sent cart")
  void testChangeStatus_OnlySentCanBeAccepted() {
    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));

    assertThatThrownBy(() -> cartService.changeStatus(testCart.getUuid(), CartStatus.ACCEPTED, itemOwner.getEmail()))
        .isInstanceOf(InvalidStatusTransitionException.class);

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when refusing non-sent cart")
  void testChangeStatus_OnlySentCanBeRefused() {
    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));

    assertThatThrownBy(() -> cartService.changeStatus(testCart.getUuid(), CartStatus.REFUSED, itemOwner.getEmail()))
        .isInstanceOf(InvalidStatusTransitionException.class);

    verify(cartRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should verify correct sequence of status transitions")
  void testChangeStatus_ValidTransitionSequence() {
    // DRAFT -> SENT
    when(cartRepository.findById(testCart.getUuid())).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    cartService.changeStatus(testCart.getUuid(), CartStatus.SENT, testUser.getEmail());
    assertThat(testCart.getStatus()).isEqualTo(CartStatus.SENT);

    // SENT -> ACCEPTED
    cartService.changeStatus(testCart.getUuid(), CartStatus.ACCEPTED, itemOwner.getEmail());
    assertThat(testCart.getStatus()).isEqualTo(CartStatus.ACCEPTED);

    verify(cartRepository, times(2)).save(testCart);
  }
}
