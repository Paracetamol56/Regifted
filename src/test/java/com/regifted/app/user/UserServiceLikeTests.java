package com.regifted.app.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.item.ItemRepository;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Like Items Logic Tests")
class UserServiceLikeTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private Item testItem;

  @BeforeEach
  void setUp() {
    testUser = createUser("user-123", "John Doe", "john@example.com");
    testItem = createItem("item-123", "Test Item", 5);
  }

  // ========================================
  // HELPER METHODS
  // ========================================

  private User createUser(String uuid, String name, String email) {
    User user = new User();
    user.setUuid(uuid);
    user.setName(name);
    user.setEmail(email);
    user.setLikedItems(new HashSet<>());
    return user;
  }

  private Item createItem(String uuid, String title, int likes) {
    Item item = new Item();
    item.setUuid(uuid);
    item.setTitle(title);
    item.setLikes(likes);
    return item;
  }

  // ========================================
  // ADD LIKE TESTS - HAPPY PATH
  // ========================================

  @Nested
  @DisplayName("addLike() - Happy Path Tests")
  class AddLikeHappyPathTests {

    @Test
    @DisplayName("Should add like to item when user hasn't liked it yet")
    void testAddLike_Success() {
      // Given
      String itemUuid = testItem.getUuid();
      int initialLikes = testItem.getLikes();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(itemRepository.save(any(Item.class))).thenReturn(testItem);
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.addLike(testUser, itemUuid);

      // Then
      assertEquals(initialLikes + 1, result.getLikes());
      assertTrue(testUser.getLikedItems().contains(testItem));
      verify(itemRepository).save(testItem);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should initialize likedItems set if null")
    void testAddLike_InitializesLikedItems() {
      // Given
      testUser.setLikedItems(null);
      String itemUuid = testItem.getUuid();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(itemRepository.save(any(Item.class))).thenReturn(testItem);
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      userService.addLike(testUser, itemUuid);

      // Then
      assertNotNull(testUser.getLikedItems());
      assertTrue(testUser.getLikedItems().contains(testItem));
    }
  }

  // ========================================
  // ADD LIKE TESTS - EDGE CASES
  // ========================================

  @Nested
  @DisplayName("addLike() - Edge Cases Tests")
  class AddLikeEdgeCasesTests {

    @Test
    @DisplayName("Should not increment likes count if already liked")
    void testAddLike_AlreadyLiked() {
      // Given
      testUser.getLikedItems().add(testItem);
      String itemUuid = testItem.getUuid();
      int initialLikes = testItem.getLikes();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.addLike(testUser, itemUuid);

      // Then
      assertEquals(initialLikes, result.getLikes());
      verify(itemRepository, never()).save(any(Item.class));
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should handle user with multiple liked items")
    void testAddLike_MultipleItems() {
      // Given
      Item anotherItem = new Item();
      anotherItem.setUuid("another-item-uuid");
      anotherItem.setLikes(5);
      testUser.getLikedItems().add(anotherItem);

      String itemUuid = testItem.getUuid();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(itemRepository.save(any(Item.class))).thenReturn(testItem);
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      userService.addLike(testUser, itemUuid);

      // Then
      assertEquals(2, testUser.getLikedItems().size());
      assertTrue(testUser.getLikedItems().contains(testItem));
      assertTrue(testUser.getLikedItems().contains(anotherItem));
    }
  }

  // ========================================
  // ADD LIKE TESTS - ERROR CASES
  // ========================================

  @Nested
  @DisplayName("addLike() - Error Cases Tests")
  class AddLikeErrorCasesTests {

    @Test
    @DisplayName("Should throw NotFoundException when item doesn't exist")
    void testAddLike_ItemNotFound() {
      // Given
      String nonExistentItemUuid = "non-existent-item";
      when(itemRepository.findById(nonExistentItemUuid)).thenReturn(Optional.empty());

      // When & Then
      NotFoundException exception = assertThrows(NotFoundException.class, () -> {
        userService.addLike(testUser, nonExistentItemUuid);
      });

      assertEquals("Not found with id: " + nonExistentItemUuid, exception.getMessage());
      verify(itemRepository, never()).save(any(Item.class));
      verify(userRepository, never()).save(any(User.class));
    }
  }

  // ========================================
  // REMOVE LIKE TESTS - HAPPY PATH
  // ========================================

  @Nested
  @DisplayName("removeLike() - Happy Path Tests")
  class RemoveLikeHappyPathTests {

    @Test
    @DisplayName("Should remove like from item when user has liked it")
    void testRemoveLike_Success() {
      // Given
      testUser.getLikedItems().add(testItem);
      testItem.setLikes(5);
      String itemUuid = testItem.getUuid();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(itemRepository.save(any(Item.class))).thenReturn(testItem);
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.removeLike(testUser, itemUuid);

      // Then
      assertEquals(4, result.getLikes());
      assertFalse(testUser.getLikedItems().contains(testItem));
      verify(itemRepository).save(testItem);
      verify(userRepository).save(testUser);
    }
  }

  // ========================================
  // REMOVE LIKE TESTS - EDGE CASES
  // ========================================

  @Nested
  @DisplayName("removeLike() - Edge Cases Tests")
  class RemoveLikeEdgeCasesTests {

    @Test
    @DisplayName("Should not decrement likes if user hasn't liked the item")
    void testRemoveLike_NotLiked() {
      // Given
      String itemUuid = testItem.getUuid();
      int initialLikes = testItem.getLikes();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.removeLike(testUser, itemUuid);

      // Then
      assertEquals(initialLikes, result.getLikes());
      verify(itemRepository, never()).save(any(Item.class));
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should handle removing like when item not in user's liked items")
    void testRemoveLike_ItemNotInLikedItems() {
      // Given
      String itemUuid = testItem.getUuid();
      int initialLikes = testItem.getLikes();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.removeLike(testUser, itemUuid);

      // Then
      assertEquals(initialLikes, result.getLikes());
      assertFalse(testUser.getLikedItems().contains(testItem));
      verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("Should handle user with multiple liked items")
    void testRemoveLike_MultipleItems() {
      // Given
      Item anotherItem = new Item();
      anotherItem.setUuid("another-item-uuid");
      anotherItem.setLikes(5);
      testUser.getLikedItems().add(testItem);
      testUser.getLikedItems().add(anotherItem);

      String itemUuid = testItem.getUuid();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(itemRepository.save(any(Item.class))).thenReturn(testItem);
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      userService.removeLike(testUser, itemUuid);

      // Then
      assertEquals(1, testUser.getLikedItems().size());
      assertFalse(testUser.getLikedItems().contains(testItem));
      assertTrue(testUser.getLikedItems().contains(anotherItem));
    }

    @Test
    @DisplayName("Should handle null likedItems gracefully")
    void testRemoveLike_NullLikedItems() {
      // Given
      testUser.setLikedItems(null);
      String itemUuid = testItem.getUuid();
      int initialLikes = testItem.getLikes();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.removeLike(testUser, itemUuid);

      // Then
      assertNotNull(testUser.getLikedItems());
      assertEquals(initialLikes, result.getLikes());
      verify(itemRepository, never()).save(any(Item.class));
    }
  }

  // ========================================
  // REMOVE LIKE TESTS - BUSINESS LOGIC
  // ========================================

  @Nested
  @DisplayName("removeLike() - Business Logic Tests")
  class RemoveLikeBusinessLogicTests {

    @Test
    @DisplayName("Should not allow negative likes count")
    void testRemoveLike_PreventNegativeLikes() {
      // Given
      testUser.getLikedItems().add(testItem);
      testItem.setLikes(0);
      String itemUuid = testItem.getUuid();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(itemRepository.save(any(Item.class))).thenReturn(testItem);
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.removeLike(testUser, itemUuid);

      // Then
      assertEquals(0, result.getLikes());
      assertFalse(testUser.getLikedItems().contains(testItem));
    }

    @Test
    @DisplayName("Should handle removing like when likes count is already 0")
    void testRemoveLike_LikesAlreadyZero() {
      // Given
      testUser.getLikedItems().add(testItem);
      testItem.setLikes(0);
      String itemUuid = testItem.getUuid();
      when(itemRepository.findById(itemUuid)).thenReturn(Optional.of(testItem));
      when(itemRepository.save(any(Item.class))).thenReturn(testItem);
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // When
      Item result = userService.removeLike(testUser, itemUuid);

      // Then
      assertEquals(0, result.getLikes());
      verify(itemRepository).save(testItem);
    }
  }

  // ========================================
  // REMOVE LIKE TESTS - ERROR CASES
  // ========================================

  @Nested
  @DisplayName("removeLike() - Error Cases Tests")
  class RemoveLikeErrorCasesTests {

    @Test
    @DisplayName("Should throw NotFoundException when item doesn't exist")
    void testRemoveLike_ItemNotFound() {
      // Given
      String nonExistentItemUuid = "non-existent-item";
      when(itemRepository.findById(nonExistentItemUuid)).thenReturn(Optional.empty());

      // When & Then
      NotFoundException exception = assertThrows(NotFoundException.class, () -> {
        userService.removeLike(testUser, nonExistentItemUuid);
      });

      assertEquals("Not found with id: " + nonExistentItemUuid, exception.getMessage());
      verify(itemRepository, never()).save(any(Item.class));
      verify(userRepository, never()).save(any(User.class));
    }
  }

  // ========================================
  // HAS LIKED ITEM TESTS
  // ========================================

  @Nested
  @DisplayName("hasLikedItem() Tests")
  class HasLikedItemTests {

    @Test
    @DisplayName("Should return true when user has liked the item")
    void testHasLikedItem_UserLikedItem() {
      // Given
      testUser.getLikedItems().add(testItem);

      // When
      boolean result = userService.hasLikedItem(testUser, testItem);

      // Then
      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user hasn't liked the item")
    void testHasLikedItem_UserNotLikedItem() {
      // Given
      // testUser.getLikedItems() is empty

      // When
      boolean result = userService.hasLikedItem(testUser, testItem);

      // Then
      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when likedItems is null")
    void testHasLikedItem_LikedItemsNull() {
      // Given
      testUser.setLikedItems(null);

      // When
      boolean result = userService.hasLikedItem(testUser, testItem);

      // Then
      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when likedItems is empty")
    void testHasLikedItem_LikedItemsEmpty() {
      // Given
      testUser.setLikedItems(new HashSet<>());

      // When
      boolean result = userService.hasLikedItem(testUser, testItem);

      // Then
      assertFalse(result);
    }

    @Test
    @DisplayName("Should handle checking multiple items for same user")
    void testHasLikedItem_MultipleItems() {
      // Given
      Item item1 = createItem("item-1", "Item 1", 10);
      Item item2 = createItem("item-2", "Item 2", 15);
      Item item3 = createItem("item-3", "Item 3", 20);

      testUser.getLikedItems().add(item1);
      testUser.getLikedItems().add(item3);

      // When & Then
      assertTrue(userService.hasLikedItem(testUser, item1));
      assertFalse(userService.hasLikedItem(testUser, item2));
      assertTrue(userService.hasLikedItem(testUser, item3));
    }
  }
}
