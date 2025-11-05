package com.regifted.app.user;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository repository;

  private User validUser;

  @BeforeEach
  void setup() {
    validUser = new User();
    validUser.setUuid("65143e3d-34d5-4d73-8f76-0324bf69229b");
    validUser.setName("John Doe");
    validUser.setEmail("john@example.com");
    validUser.setPassword("$2a$12$BFnfxxy2fgNMe6IUr/80mOTfk5ZwmbjVZBBJsd/yf53HWzJ0azLEa"); // bcrypt hash for
                                                                                           // "AdminPass123!"
    validUser.setNotification(true);
    validUser.setCreatedAt(Instant.now());
    validUser.setUpdatedAt(Instant.now());
  }

  @Test
  @DisplayName("Get user by UUID returns user")
  void testGetByUuid() {
    when(repository.findById(validUser.getUuid()))
        .thenReturn(Optional.of(validUser));

    // Log UUID
    System.out.println(">>> Testing getByUuid with UUID: " + validUser.getUuid());

    User fetchedUser = userService.getByUuid(validUser.getUuid());

    assertNotNull(fetchedUser);
    assertEquals(validUser.getUuid(), fetchedUser.getUuid());
    assertEquals(validUser.getEmail(), fetchedUser.getEmail());
  }

}
