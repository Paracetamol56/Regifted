package com.regifted.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.regifted.app.user.User;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

  private User user;
  private CustomUserDetails userDetails;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setUuid("user-123");
    user.setName("John Doe");
    user.setEmail("john@example.com");
    user.setPassword("hashedPassword");
    userDetails = new CustomUserDetails(user);
  }

  @Test
  void testGetUserReturnsUser() {
    assertEquals(user, userDetails.getUser());
  }

  @Test
  void testGetAuthoritiesReturnsNull() {
    Collection<?> authorities = userDetails.getAuthorities();
    assertNull(authorities);
  }

  @Test
  void testGetPasswordReturnsUserPassword() {
    assertEquals("hashedPassword", userDetails.getPassword());
  }

  @Test
  void testGetUsernameReturnsUserEmail() {
    assertEquals("john@example.com", userDetails.getUsername());
  }

  @Test
  void testAccountStatusMethodsReturnTrue() {
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isEnabled());
  }
}
