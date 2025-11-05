package com.regifted.app.service;

import com.regifted.app.dto.LoginRequest;
import com.regifted.app.dto.LoginResponse;
import com.regifted.app.dto.RegisterRequest;
import com.regifted.app.entity.User;
import com.regifted.app.repository.UserRepository;
import com.regifted.app.security.JwtUtils;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.regifted.app.dto.LoginResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository repository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private JwtUtils jwtUtils;

  private User validUser;

  @BeforeEach
  void setup() {
    passwordEncoder = new BCryptPasswordEncoder();

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

  @Nested
  @DisplayName("Register Tests")
  class RegisterTests {

    @Test
    @DisplayName("Successful registration")
    void testRegisterSuccess() {
      when(repository.existsByEmail(validUser.getEmail()))
          .thenReturn(false);
      when(repository.save(any(User.class)))
          .thenReturn(validUser);

      RegisterRequest request = new RegisterRequest(validUser.getName(), validUser.getEmail(), "StrongPass123!");
      LoginResponse created = userService.register(request);

      System.out.println(">>> Created JWT: " + created.getToken());
      assertNotNull(created);
    }

    @Test
    @DisplayName("Registration fails with existing email")
    void testRegisterExistingEmail() {
      when(repository.existsByEmail(validUser.getEmail())).thenReturn(true);

      RegisterRequest request = new RegisterRequest(validUser.getName(), validUser.getEmail(), "StrongPass123!");
      Exception exception = assertThrows(RuntimeException.class, () -> userService.register(request));

      System.out.println(">>> Exception message: " + exception.getMessage());
      assertTrue(exception.getMessage().contains("Email already in use"));
    }

    @Test
    @DisplayName("Registration fails with empty or blank fields")
    void testRegisterEmptyFields() {
      assertThrows(RuntimeException.class,
          () -> userService.register(new RegisterRequest("", "test@example.com", "password")));
      assertThrows(RuntimeException.class, () -> userService.register(new RegisterRequest("John", "", "password")));
      assertThrows(RuntimeException.class,
          () -> userService.register(new RegisterRequest("John", "test@example.com", "")));
    }

    @Test
    @DisplayName("Registration fails with invalid email")
    void testRegisterInvalidEmail() {
      assertThrows(RuntimeException.class,
          () -> userService.register(new RegisterRequest("John", "invalid-email", "password")));
    }
  }

  @Nested
  @DisplayName("Login Tests")
  class LoginTests {

    @Test
    @DisplayName("Successful login returns JWT")
    void testLoginSuccess() {
      when(repository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));

      LoginRequest request = new LoginRequest(validUser.getEmail(), "AdminPass123!");
      LoginResponse response = userService.login(request);

      System.out.println(">>> Login JWT: " + response.getToken());

      assertNotNull(response);
      assertNotNull(response.getToken());
      assertEquals("Bearer", response.getTokenType());
    }

    @Test
    @DisplayName("Login fails with incorrect password")
    void testLoginIncorrectPassword() {
      when(repository.findByEmail(validUser.getEmail())).thenReturn(Optional.of(validUser));

      LoginRequest request = new LoginRequest(validUser.getEmail(), "WrongPassword");
      Exception exception = assertThrows(RuntimeException.class, () -> userService.login(request));

      assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    @DisplayName("Login fails with non-existent email")
    void testLoginNonExistentEmail() {
      when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

      LoginRequest request = new LoginRequest("missing@example.com", "password");
      Exception exception = assertThrows(RuntimeException.class, () -> userService.login(request));

      assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    @DisplayName("Login fails with empty or blank fields")
    void testLoginEmptyFields() {
      assertThrows(RuntimeException.class, () -> userService.login(new LoginRequest("", "password")));
      assertThrows(RuntimeException.class, () -> userService.login(new LoginRequest("test@example.com", "")));
    }
  }
}
