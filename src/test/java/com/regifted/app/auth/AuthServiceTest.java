package com.regifted.app.auth;

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

import com.regifted.app.auth.dto.LoginRequest;
import com.regifted.app.auth.dto.LoginResponse;
import com.regifted.app.auth.dto.RegisterRequest;
import com.regifted.app.user.User;
import com.regifted.app.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

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
      LoginResponse created = authService.register(request);

      System.out.println(">>> Created JWT: " + created.getToken());
      assertNotNull(created);
    }

    @Test
    @DisplayName("Registration fails with existing email")
    void testRegisterExistingEmail() {
      when(repository.existsByEmail(validUser.getEmail())).thenReturn(true);

      RegisterRequest request = new RegisterRequest(validUser.getName(), validUser.getEmail(), "StrongPass123!");
      Exception exception = assertThrows(RuntimeException.class, () -> authService.register(request));

      System.out.println(">>> Exception message: " + exception.getMessage());
      assertTrue(exception.getMessage().contains("Email already in use"));
    }

    @Test
    @DisplayName("Registration fails with empty or blank fields")
    void testRegisterEmptyFields() {
      assertThrows(RuntimeException.class,
          () -> authService.register(new RegisterRequest("", "test@example.com", "password")));
      assertThrows(RuntimeException.class, () -> authService.register(new RegisterRequest("John", "", "password")));
      assertThrows(RuntimeException.class,
          () -> authService.register(new RegisterRequest("John", "test@example.com", "")));
    }

    @Test
    @DisplayName("Registration fails with invalid email")
    void testRegisterInvalidEmail() {
      assertThrows(RuntimeException.class,
          () -> authService.register(new RegisterRequest("John", "invalid-email", "password")));
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
      LoginResponse response = authService.login(request);

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
      Exception exception = assertThrows(RuntimeException.class, () -> authService.login(request));

      assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    @DisplayName("Login fails with non-existent email")
    void testLoginNonExistentEmail() {
      when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

      LoginRequest request = new LoginRequest("missing@example.com", "password");
      Exception exception = assertThrows(RuntimeException.class, () -> authService.login(request));

      assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    @DisplayName("Login fails with empty or blank fields")
    void testLoginEmptyFields() {
      assertThrows(RuntimeException.class, () -> authService.login(new LoginRequest("", "password")));
      assertThrows(RuntimeException.class, () -> authService.login(new LoginRequest("test@example.com", "")));
    }
  }
}
