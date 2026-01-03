package com.regifted.app.item;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordService;
import com.regifted.app.security.CustomUserDetailsService;
import com.regifted.app.security.SecurityConfig;
import com.regifted.app.security.WithMockCustomUser;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;
import com.regifted.app.bundle.BundleService;
import com.regifted.app.exception.NotFoundException;

@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)
public class ItemControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ItemService itemService;

  @MockBean
  private KeywordService keywordService;

  @MockBean
  private UserService userService;

  @MockBean
  private BundleService bundleService;

  @MockBean
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private User mockUser;
  private Item mockItem;
  private Keyword mockKeyword;
  private static final String TEST_EMAIL = "test@example.com";

  @BeforeEach
  void setUp() {
    // Utilisateur
    mockUser = new User();
    mockUser.setEmail(TEST_EMAIL);
    mockUser.setUuid(UUID.randomUUID().toString());

    // Item
    mockItem = new Item();
    mockItem.setUuid("item-123");
    mockItem.setTitle("Objet de Test");
    mockItem.setDescription("Description de test");
    mockItem.setUser(mockUser);
    mockItem.setLatitude(48.8566f);
    mockItem.setLongitude(2.3522f);
    mockItem.setState(EState.USED);

    // Keyword nécessaire pour Thymeleaf
    mockKeyword = new Keyword();
    mockKeyword.setItems(Set.of(mockItem));
    mockItem.setKeywords(Set.of(mockKeyword));
  }

  // =========================================================================
  // CRÉATION D'ITEMS (POST)
  // =========================================================================

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("POST JSON: Succès")
  void createItem_Json_Success() throws Exception {
    ItemPostRequest req = new ItemPostRequest();
    req.setTitle("Nouveau");
    req.setDescription("Description valide");
    req.setLatitude(48.8566f);
    req.setLongitude(2.3522f);
    req.setState(EState.USED);

    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);
    when(itemService.createItem(any(ItemPostRequest.class), eq(mockUser))).thenReturn(mockItem);

    mockMvc.perform(post("/items")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value("item-123"));
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("POST Web: Succès Redirect vers détails")
  void createItem_Html_Success() throws Exception {
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);
    when(itemService.createItem(any(ItemPostRequest.class), eq(mockUser))).thenReturn(mockItem);

    mockMvc.perform(post("/items")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("title", "Titre")
        .param("description", "Description")
        .param("latitude", "48.8566")
        .param("longitude", "2.3522")
        .param("state", EState.USED.toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/items/" + mockItem.getUuid()));
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("POST JSON: Échec validation (400 Bad Request)")
  void createItem_Json_Invalid_Returns400() throws Exception {
    mockMvc.perform(post("/items")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST JSON: Non authentifié (401 Unauthorized)")
  void createItem_NoAuth_Returns401() throws Exception {
    ItemPostRequest req = new ItemPostRequest();
    req.setTitle("Test");

    mockMvc.perform(post("/items")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized());
  }

  // =========================================================================
  // LISTE / RECHERCHE D'ITEMS (GET)
  // =========================================================================

  @Test
  @DisplayName("GET HTML: Liste avec filtres et pagination")
  void listItems_Html_Success() throws Exception {
    when(itemService.getItemSearchPage(anyInt(), anyInt(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(mockItem)));

    mockMvc.perform(get("/items")
        .param("page", "1")
        .param("q", "search")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("items/index"))
        .andExpect(model().attributeExists("items", "keywords", "totalPages"));
  }

  @Test
  @DisplayName("GET JSON: Liste paginée")
  void listItems_Json_Success() throws Exception {
    when(itemService.getItemSearchPage(anyInt(), anyInt(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(mockItem)));

    mockMvc.perform(get("/items")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").exists());
  }

  // =========================================================================
  // CONSULTATION UNITAIRE (GET /items/{uuid})
  // =========================================================================

  @Test
  @DisplayName("GET JSON: Détails par UUID")
  void getItem_Json_Success() throws Exception {
    when(itemService.getById("item-123")).thenReturn(mockItem);

    mockMvc.perform(get("/items/item-123")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value("item-123"));
  }

  @Test
  @DisplayName("GET HTML: Utilisateur anonyme")
  void getItem_Html_Anonymous() throws Exception {
    when(itemService.getById("item-123")).thenReturn(mockItem);

    mockMvc.perform(get("/items/item-123")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(model().attribute("isOwner", false))
        .andExpect(model().attribute("liked", false));
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("GET HTML: Propriétaire connecté")
  void getItem_Html_Owner() throws Exception {
    when(itemService.getById("item-123")).thenReturn(mockItem);
    when(userService.getByEmail(TEST_EMAIL)).thenReturn(mockUser);

    mockMvc.perform(get("/items/item-123")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(model().attribute("isOwner", true));
  }

  @Test
  @DisplayName("GET: Objet inexistant retourne 404")
  void getItem_NotFound_Returns404() throws Exception {
    when(itemService.getById("void")).thenThrow(new NotFoundException("void"));

    mockMvc.perform(get("/items/void")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  // =========================================================================
  // MISE À JOUR (PUT)
  // =========================================================================

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("PUT JSON: Succès")
  void updateItem_Json_Success() throws Exception {
    ItemPutRequest putReq = new ItemPutRequest();
    putReq.setTitle("Titre Modifié");
    putReq.setDescription(mockItem.getDescription());
    putReq.setLatitude(mockItem.getLatitude());
    putReq.setLongitude(mockItem.getLongitude());
    putReq.setState(mockItem.getState());

    when(itemService.updateItemById(eq("item-123"), any())).thenReturn(mockItem);

    mockMvc.perform(put("/items/item-123")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(putReq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value("item-123"));
  }

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("PUT Web: Succès et redirection")
  void updateItem_Html_Success() throws Exception {
    when(itemService.updateItemById(eq("item-123"), any())).thenReturn(mockItem);

    mockMvc.perform(put("/items/item-123")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("title", mockItem.getTitle())
        .param("description", mockItem.getDescription())
        .param("latitude", String.valueOf(mockItem.getLatitude()))
        .param("longitude", String.valueOf(mockItem.getLongitude()))
        .param("state", mockItem.getState().toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/items/item-123"));
  }

  // =========================================================================
  // SUPPRESSION (DELETE)
  // =========================================================================

  @Test
  @WithMockCustomUser(username = TEST_EMAIL)
  @DisplayName("DELETE: Succès avec header HTMX")
  void deleteItem_Success_WithHtmxHeader() throws Exception {
    mockMvc.perform(delete("/items/item-123"))
        .andExpect(status().isOk())
        .andExpect(header().string("HX-Redirect", "/items"));

    verify(itemService, times(1)).deleteItemById("item-123");
  }

  // =========================================================================
  // FORMULAIRES (GET /items/update/{uuid})
  // =========================================================================

  @Test
  @DisplayName("GET Form: Rendu du formulaire d'update")
  void getUpdateForm_Success() throws Exception {
    when(itemService.getById("item-123")).thenReturn(mockItem);

    mockMvc.perform(get("/items/update/item-123"))
        .andExpect(status().isOk())
        .andExpect(view().name("update-item"))
        .andExpect(model().attributeExists("item"));
  }

  // =========================================================================
  // CAS D'ÉCHEC AUTHENTIFICATION / AUTORISATION
  // =========================================================================

  @Test
  @DisplayName("PUT JSON: Non authentifié retourne 401")
  void updateItem_Json_NoAuth_Returns401() throws Exception {
    ItemPutRequest putReq = new ItemPutRequest();
    putReq.setTitle("Test");

    mockMvc.perform(put("/items/item-123")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(putReq)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("DELETE: Non authentifié retourne 401")
  void deleteItem_NoAuth_Returns401() throws Exception {
    mockMvc.perform(delete("/items/item-123"))
        .andExpect(status().isUnauthorized());
  }
}
