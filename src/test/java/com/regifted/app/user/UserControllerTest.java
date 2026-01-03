package com.regifted.app.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regifted.app.item.Item;
import com.regifted.app.security.CustomUserDetailsService;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.security.SecurityConfig;
import com.regifted.app.security.WithMockCustomUser;
import com.regifted.app.user.dto.UserPostRequest;

import com.regifted.app.exception.NotFoundException;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @MockBean
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private User mockUser;
  private CustomUserPrincipal mockPrincipal;
  private Item mockItem;

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_PASSWORD = "password123";

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setUuid(UUID.randomUUID().toString());
    mockUser.setName("Test User");
    mockUser.setEmail(TEST_EMAIL);
    mockUser.setPassword("$2a$10$encodedPassword"); // BCrypt encoded
    mockUser.setCreatedAt(Instant.now());
    mockUser.setItems(new HashSet<>());
    mockUser.setLikedItems(new HashSet<>());

    mockPrincipal = new CustomUserPrincipal(mockUser);

    mockItem = new Item();
    mockItem.setUuid("item-123");
    mockItem.setTitle("Test Item");

    // Mock du service d'authentification pour HTTP Basic
    when(userDetailsService.loadUserByUsername(TEST_EMAIL))
        .thenReturn(mockPrincipal);
  }

  // =============================
  // TESTS CREATION (POST /users)
  // =============================

  @Test
  @DisplayName("API POST: Création JSON réussie - endpoint public")
  void createUserApi_Success() throws Exception {

    UserPostRequest request = new UserPostRequest();
    request.setName("Test User");
    request.setEmail("test@example.com");
    request.setPassword("password123");
    request.setNotification(true);

    when(userService.createUser(any(UserPostRequest.class)))
        .thenReturn(mockUser);

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated()) // 201
        .andExpect(jsonPath("$.uuid").value(mockUser.getUuid()))
        .andExpect(jsonPath("$.name").value(mockUser.getName()))
        .andExpect(jsonPath("$.email").value(mockUser.getEmail()));

    verify(userService, times(1))
        .createUser(any(UserPostRequest.class));
  }

  @Test
  @DisplayName("Web POST: Inscription via formulaire HTML - endpoint public")
  void registerFromWeb_Success() throws Exception {
    when(userService.createUser(any(UserPostRequest.class))).thenReturn(mockUser);

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("name", "Test User")
        .param("email", TEST_EMAIL)
        .param("password", TEST_PASSWORD)
        .param("notification", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("users/me"))
        .andExpect(model().attributeExists("user"))
        .andExpect(model().attribute("user", mockUser));

    verify(userService, times(1)).createUser(any(UserPostRequest.class));
  }

  @Test
  @DisplayName("API POST: Validation échoue avec données invalides")
  void createUserApi_ValidationFails() throws Exception {
    String invalidJson = "{\"name\":\"\",\"email\":\"invalid-email\",\"password\":\"short\"}";

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  // =============================
  // TESTS PROFIL (/me) AVEC HTTP BASIC
  // =============================
  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("Web GET /me: Profil HTML avec utilisateur authentifié")
  void getMe_WithMockUser_Success() throws Exception {
    // Mock du service pour retourner l'utilisateur
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);

    mockMvc.perform(get("/users/me")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("users/me"))
        .andExpect(model().attribute("user", mockUser));

    verify(userService, times(1)).getByEmail(TEST_EMAIL);
  }

  // @TODO REVOIR
  @Test
  @DisplayName("Web GET /me: Non authentifié retourne 302")
  void getMe_NotAuthenticated_Returns401() throws Exception {
    mockMvc.perform(get("/users/me")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Web GET /me: Credentials invalides retourne 401")
  void getMe_InvalidCredentials_Returns401() throws Exception {
    when(userDetailsService.loadUserByUsername("wrong@example.com"))
        .thenThrow(new UsernameNotFoundException("User not found"));

    mockMvc.perform(get("/users/me")
        .with(httpBasic("wrong@example.com", "wrongpassword"))
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("API GET /me: Profil JSON avec HTTP Basic authentifié")
  @WithMockCustomUser(username = TEST_EMAIL)
  void getMeApi_WithHttpBasic_Success() throws Exception {
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);

    mockMvc.perform(get("/users/me")
        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value(mockUser.getUuid()))
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.email").value(TEST_EMAIL));

    verify(userService, times(1)).getByEmail(TEST_EMAIL);
  }

  @Test
  @DisplayName("API GET /me: Non authentifié retourne 401")
  void getMeApi_NotAuthenticated_Returns401() throws Exception {
    mockMvc.perform(get("/users/me")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  // =============================
  // TESTS FAVORIS (LIKES) AVEC HTTP BASIC
  // =============================

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("POST /me/likes: Ajout like avec HTTP Basic authentifié")
  void addLike_WithHttpBasic_Success() throws Exception {
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);
    when(userService.addLike(any(User.class), eq("item-123"))).thenReturn(mockItem);

    mockMvc.perform(post("/users/me/likes")
        .param("item", "item-123"))
        .andExpect(status().isOk())
        .andExpect(view().name("items/like-button"))
        .andExpect(model().attribute("liked", true))
        .andExpect(model().attribute("item", mockItem));

    verify(userService, times(1)).getByEmail(TEST_EMAIL);
    verify(userService, times(1)).addLike(any(User.class), eq("item-123"));
  }

  @Test
  @DisplayName("POST /me/likes: Non authentifié retourne 401")
  void addLike_NotAuthenticated_Returns401() throws Exception {
    mockMvc.perform(post("/users/me/likes")
        .param("item", "item-123"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("POST /me/likes: Sans paramètre item retourne 400")
  void addLike_MissingItemParameter_Returns400() throws Exception {
    mockMvc.perform(post("/users/me/likes"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("DELETE /me/likes: Suppression like avec HTTP Basic authentifié")
  void removeLike_WithHttpBasic_Success() throws Exception {
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);
    when(userService.removeLike(any(User.class), eq("item-123"))).thenReturn(mockItem);

    mockMvc.perform(delete("/users/me/likes")
        .param("item", "item-123"))
        .andExpect(status().isOk())
        .andExpect(view().name("items/like-button"))
        .andExpect(model().attribute("liked", false))
        .andExpect(model().attribute("item", mockItem));

    verify(userService, times(1)).getByEmail(TEST_EMAIL);
    verify(userService, times(1)).removeLike(any(User.class), eq("item-123"));
  }

  @Test
  @DisplayName("DELETE /me/likes: Non authentifié retourne 401")
  void removeLike_NotAuthenticated_Returns401() throws Exception {
    mockMvc.perform(delete("/users/me/likes")
        .param("item", "item-123"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("DELETE /me/likes: Sans paramètre item retourne 400")
  void removeLike_MissingItemParameter_Returns400() throws Exception {
    mockMvc.perform(delete("/users/me/likes")
        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
        .andExpect(status().isBadRequest());
  }

  // =============================
  // TESTS UUID PUBLIC (SANS AUTH)
  // =============================

  @Test
  @DisplayName("Web GET /{uuid}: Profil public HTML - endpoint public")
  void getUserHtml_PublicEndpoint_Success() throws Exception {
    String uuid = mockUser.getUuid();
    when(userService.getByUuid(uuid)).thenReturn(mockUser);

    mockMvc.perform(get("/users/" + uuid)
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("users/{uuid}"))
        .andExpect(model().attributeExists("user"))
        .andExpect(model().attribute("user", mockUser));

    verify(userService, times(1)).getByUuid(uuid);
  }

  @Test
  @DisplayName("API GET /{uuid}: Profil public JSON - endpoint public")
  void getUserApi_PublicEndpoint_Success() throws Exception {
    String uuid = mockUser.getUuid();
    when(userService.getByUuid(uuid)).thenReturn(mockUser);

    mockMvc.perform(get("/users/" + uuid)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value(uuid))
        .andExpect(jsonPath("$.name").value(mockUser.getName()));

    verify(userService, times(1)).getByUuid(uuid);
  }

  @Test
  @DisplayName("API GET /{uuid}: UUID invalide retourne 404")
  void getUserApi_InvalidUuid_Returns404() throws Exception {
    String invalidUuid = "invalid-uuid";

    // Simule NotFoundException au lieu de RuntimeException
    when(userService.getByUuid(invalidUuid))
        .thenThrow(new NotFoundException(invalidUuid));

    mockMvc.perform(get("/users/" + invalidUuid)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Web GET /{uuid}: Accessible sans authentification")
  void getUserHtml_NoAuthenticationRequired() throws Exception {
    String uuid = mockUser.getUuid();
    when(userService.getByUuid(uuid)).thenReturn(mockUser);

    mockMvc.perform(get("/users/" + uuid)
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("users/{uuid}"));
  }

  // =============================
  // TESTS CONTENT NEGOTIATION
  // =============================

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("GET /me: Retourne HTML par défaut avec HTTP Basic")
  void getMe_DefaultContentType_ReturnsHtml() throws Exception {
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);

    mockMvc.perform(get("/users/me")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("users/me"))
        .andExpect(model().attribute("user", mockUser));
  }

  @Test
  @DisplayName("GET /{uuid}: Content negotiation avec Accept headers")
  void getUserWithDifferentAcceptHeaders() throws Exception {
    String uuid = mockUser.getUuid();
    when(userService.getByUuid(uuid)).thenReturn(mockUser);

    // Test avec XML
    mockMvc.perform(get("/users/" + uuid)
        .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk());

    // Test avec JSON
    mockMvc.perform(get("/users/" + uuid)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").exists());
  }

  // =============================
  // TESTS EDGE CASES
  // =============================

  @Test
  @DisplayName("POST /users: Email dupliqué retourne erreur")
  void createUser_DuplicateEmail_ReturnsError() throws Exception {
    UserPostRequest req = new UserPostRequest();
    req.setName("Test User");
    req.setEmail("existing@example.com");
    req.setPassword(TEST_PASSWORD);

    when(userService.createUser(any(UserPostRequest.class)))
        .thenThrow(new RuntimeException("Email already exists"));

    mockMvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("POST /me/likes: Item déjà liké")
  void addLike_AlreadyLiked_HandlesGracefully() throws Exception {
    Set<Item> likedItems = new HashSet<>();
    likedItems.add(mockItem);
    mockUser.setLikedItems(likedItems);

    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);
    when(userService.addLike(any(User.class), eq("item-123"))).thenReturn(mockItem);

    mockMvc.perform(post("/users/me/likes")
        .param("item", "item-123"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("liked", true));
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("DELETE /me/likes: Item non liké")
  void removeLike_NotLiked_HandlesGracefully() throws Exception {
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);
    when(userService.removeLike(any(User.class), eq("item-999"))).thenReturn(mockItem);

    mockMvc.perform(delete("/users/me/likes")
        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
        .param("item", "item-999"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("liked", false));
  }
}
