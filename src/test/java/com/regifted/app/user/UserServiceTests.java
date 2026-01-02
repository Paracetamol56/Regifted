package com.regifted.app.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.regifted.app.item.Item;
import com.regifted.app.item.ItemRepository;
import com.regifted.app.user.dto.UserPostRequest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.regifted.app.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private UserPostRequest testUserPostRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setUuid("user-123");
    testUser.setEmail("test@example.com");
    testUser.setName("John Doe");

    testUserPostRequest = new UserPostRequest();
    testUserPostRequest.setEmail("newuser@example.com");
    testUserPostRequest.setName("Jane Smith");
    testUserPostRequest.setPassword("rawPassword123");
  }

  // ========================================
  // CREATE USER TESTS
  // ========================================

  @Nested
  @DisplayName("createUser() Tests")
  class CreateUserTests {

    @Test
    @DisplayName("Should create user successfully with valid data")
    void testCreateUser_Success() {
      // Given
      String encodedPassword = "encodedPassword123";
      when(passwordEncoder.encode(testUserPostRequest.getPassword())).thenReturn(encodedPassword);
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      User result = userService.createUser(testUserPostRequest);

      // Then
      assertNotNull(result);
      assertEquals(testUserPostRequest.getEmail(), result.getEmail());
      assertEquals(testUserPostRequest.getName(), result.getName());
      assertEquals(encodedPassword, result.getPassword());
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should encode password before saving")
    void testCreateUser_PasswordEncoded() {
      // Given
      String rawPassword = "mySecretPassword";
      String encodedPassword = "encoded_mySecretPassword";
      testUserPostRequest.setPassword(rawPassword);

      when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      User result = userService.createUser(testUserPostRequest);

      // Then
      verify(passwordEncoder).encode(rawPassword);
      assertEquals(encodedPassword, result.getPassword());
      assertNotEquals(rawPassword, result.getPassword());
    }

    @Test
    @DisplayName("Should save user with all fields from UserPostRequest")
    void testCreateUser_AllFieldsMapped() {
      // Given
      when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      User result = userService.createUser(testUserPostRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());

      User savedUser = userCaptor.getValue();
      assertEquals(testUserPostRequest.getEmail(), savedUser.getEmail());
      assertEquals(testUserPostRequest.getName(), savedUser.getName());
    }

    @Test
    @DisplayName("Should handle user creation with minimal required fields")
    void testCreateUser_MinimalFields() {
      // Given
      UserPostRequest minimalRequest = new UserPostRequest();
      minimalRequest.setEmail("minimal@example.com");
      minimalRequest.setPassword("password");

      when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      User result = userService.createUser(minimalRequest);

      // Then
      assertNotNull(result);
      assertEquals(minimalRequest.getEmail(), result.getEmail());
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should call repository save exactly once")
    void testCreateUser_SaveCalledOnce() {
      // Given
      when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

      // When
      userService.createUser(testUserPostRequest);

      // Then
      verify(userRepository, times(1)).save(any(User.class));
    }
  }

  // ========================================
  // GET BY EMAIL TESTS
  // ========================================

  @Nested
  @DisplayName("getByEmail() Tests")
  class GetByEmailTests {

    @Test
    @DisplayName("Should return user when email exists")
    void testGetByEmail_UserExists() {
      // Given
      String email = "test@example.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

      // When
      User result = userService.getByEmail(email);

      // Then
      assertNotNull(result);
      assertEquals(testUser.getUuid(), result.getUuid());
      assertEquals(testUser.getEmail(), result.getEmail());
      verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should return null when email doesn't exist")
    void testGetByEmail_UserNotFound() {
      // Given
      String email = "nonexistent@example.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      // When
      User result = userService.getByEmail(email);

      // Then
      assertNull(result);
      verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should handle case-sensitive email lookup correctly")
    void testGetByEmail_CaseSensitive() {
      // Given
      String email = "Test@Example.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

      // When
      User result = userService.getByEmail(email);

      // Then
      assertNotNull(result);
      verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should handle null email parameter")
    void testGetByEmail_NullEmail() {
      // Given
      when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

      // When
      User result = userService.getByEmail(null);

      // Then
      assertNull(result);
      verify(userRepository).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle empty string email")
    void testGetByEmail_EmptyEmail() {
      // Given
      String emptyEmail = "";
      when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

      // When
      User result = userService.getByEmail(emptyEmail);

      // Then
      assertNull(result);
      verify(userRepository).findByEmail(emptyEmail);
    }
  }

  // ========================================
  // GET BY UUID TESTS
  // ========================================

  @Nested
  @DisplayName("getByUuid() Tests")
  class GetByUuidTests {

    @Test
    @DisplayName("Should return user when UUID exists")
    void testGetByUuid_UserExists() {
      // Given
      String uuid = "user-123";
      when(userRepository.findById(uuid)).thenReturn(Optional.of(testUser));

      // When
      User result = userService.getByUuid(uuid);

      // Then
      assertNotNull(result);
      assertEquals(testUser.getUuid(), result.getUuid());
      assertEquals(testUser.getEmail(), result.getEmail());
      verify(userRepository).findById(uuid);
    }

    @Test
    @DisplayName("Should throw NotFoundException when UUID doesn't exist")
    void testGetByUuid_UserNotFound_ThrowsException() {
        // Given
        String uuid = "nonexistent-uuid";
        when(userRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            userService.getByUuid(uuid);
        });

        verify(userRepository).findById(uuid);
    }

    @Test
    @DisplayName("Should handle invalid UUID format gracefully")
    void testGetByUuid_InvalidFormat() {
      // Given
      String invalidUuid = "not-a-valid-uuid-format";
      when(userRepository.findById(invalidUuid)).thenReturn(Optional.empty());

      // When
      assertThrows(NotFoundException.class, () -> {
          userService.getByUuid(invalidUuid);
      });
      verify(userRepository).findById(invalidUuid);
    }

    @Test
    @DisplayName("Should handle null UUID parameter")
    void testGetByUuid_NullUuid() {
      // Given
      when(userRepository.findById(null)).thenReturn(Optional.empty());

      // When
      assertThrows(NotFoundException.class, () -> {
          userService.getByUuid(null);
      });
      verify(userRepository).findById(null);
    }

    @Test
    @DisplayName("Should call repository findById exactly once")
    void testGetByUuid_RepositoryCalledOnce() {
      // Given
      String uuid = "user-123";
      when(userRepository.findById(uuid)).thenReturn(Optional.of(testUser));

      // When
      userService.getByUuid(uuid);

      // Then
      verify(userRepository, times(1)).findById(uuid);
    }
  }

  // ========================================
  // HAS LIKED ITEM TESTS
  // ========================================

  @Nested
  @DisplayName("hasLikedItem() Tests")
  class HasLikedItemTests {

    private Item testItem;

    @BeforeEach
    void setUp() {
      testItem = new Item();
      testItem.setUuid("item-123");
      testItem.setTitle("Test Item");
    }

    @Test
    @DisplayName("Should return true when user has liked the item")
    void testHasLikedItem_UserLikedItem() {
      // Given
      testUser.setLikedItems(new java.util.HashSet<>());
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
      testUser.setLikedItems(new java.util.HashSet<>());

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
      testUser.setLikedItems(new java.util.HashSet<>());

      // When
      boolean result = userService.hasLikedItem(testUser, testItem);

      // Then
      assertFalse(result);
    }

    @Test
    @DisplayName("Should handle checking multiple items for same user")
    void testHasLikedItem_MultipleItems() {
      // Given
      Item item1 = new Item();
      item1.setUuid("item-1");

      Item item2 = new Item();
      item2.setUuid("item-2");

      Item item3 = new Item();
      item3.setUuid("item-3");

      testUser.setLikedItems(new java.util.HashSet<>());
      testUser.getLikedItems().add(item1);
      testUser.getLikedItems().add(item3);

      // When & Then
      assertTrue(userService.hasLikedItem(testUser, item1));
      assertFalse(userService.hasLikedItem(testUser, item2));
      assertTrue(userService.hasLikedItem(testUser, item3));
    }

    @Test
    @DisplayName("Should not modify user or item when checking like status")
    void testHasLikedItem_NoSideEffects() {
      // Given
      testUser.setLikedItems(new java.util.HashSet<>());
      testUser.getLikedItems().add(testItem);
      int initialSize = testUser.getLikedItems().size();

      // When
      userService.hasLikedItem(testUser, testItem);

      // Then
      assertEquals(initialSize, testUser.getLikedItems().size());
      verifyNoInteractions(userRepository);
      verifyNoInteractions(itemRepository);
    }
  }
}
