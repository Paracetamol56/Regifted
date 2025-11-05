package com.regifted.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.regifted.app.user.User;
import com.regifted.app.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

  private UserRepository userRepository;
  private CustomUserDetailsService service;

  private User validUser;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    service = new CustomUserDetailsService(userRepository);

    validUser = new User();
    validUser.setUuid("user-123");
    validUser.setName("John Doe");
    validUser.setEmail("john@example.com");
    validUser.setPassword("hashedPassword");
  }

  @Test
  void testLoadUserByUsernameSuccess() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(validUser));

    UserDetails userDetails = service.loadUserByUsername("john@example.com");

    assertNotNull(userDetails);
    assertEquals("john@example.com", userDetails.getUsername());
    assertEquals("hashedPassword", userDetails.getPassword());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isEnabled());
  }

  @Test
  void testLoadUserByUsernameNotFound() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    UsernameNotFoundException ex = assertThrows(
        UsernameNotFoundException.class,
        () -> service.loadUserByUsername("missing@example.com"));

    assertTrue(ex.getMessage().contains("User not found"));
  }

  @Test
  void testLoadUserEntityByUuidFound() {
    when(userRepository.findById("user-123")).thenReturn(Optional.of(validUser));

    User user = service.loadUserEntityByUuid("user-123");

    assertNotNull(user);
    assertEquals("user-123", user.getUuid());
  }

  @Test
  void testLoadUserEntityByUuidNotFound() {
    when(userRepository.findById("missing-uuid")).thenReturn(Optional.empty());

    User user = service.loadUserEntityByUuid("missing-uuid");

    assertNull(user);
  }
}
