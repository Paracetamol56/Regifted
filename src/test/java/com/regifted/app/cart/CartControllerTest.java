package com.regifted.app.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regifted.app.cart.dto.CartGetResponse;
import com.regifted.app.cart.dto.CartItemPostRequest;
import com.regifted.app.cart.dto.CartPatchRequest;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

@WebMvcTest(CartController.class)
@DisplayName("CartController Integration Tests")
class CartControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CartService cartService;

  @MockBean
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  private CustomUserPrincipal mockPrincipal;
  private User mockUser;
  private User mockOwner;
  private Cart mockCart;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setUuid("user-123");
    mockUser.setEmail("test@example.com");

    mockOwner = new User();
    mockOwner.setUuid("owner-456");
    mockOwner.setEmail("owner@example.com");

    mockCart = new Cart();
    mockCart.setUuid("cart-123");
    mockCart.setUser(mockUser);
    mockCart.setOwner(mockOwner);
    mockCart.setStatus(CartStatus.DRAFT);
    mockCart.setItems(new HashSet<>());

    mockPrincipal = new CustomUserPrincipal(mockUser);
  }

  // =============================
  // TESTS GET CURRENT CART (ALL)
  // =============================

  @Test
  @DisplayName("GET /users/me/carts with JSON should return paged carts")
  void getCurrentCart_Api_ShouldReturnPagedJson() throws Exception {
    Page<Cart> page = new PageImpl<>(Collections.singletonList(mockCart));
    when(cartService.getCartsForUser(eq("test@example.com"), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/users/me/carts")
        .with(user(mockPrincipal))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].uuid").value("cart-123"));

    verify(cartService, times(1)).getCartsForUser(eq("test@example.com"), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /users/me/carts with XML should return paged carts")
  void getCurrentCart_Api_ShouldReturnPagedXml() throws Exception {
    Page<Cart> page = new PageImpl<>(Collections.singletonList(mockCart));
    when(cartService.getCartsForUser(eq("test@example.com"), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/users/me/carts")
        .with(user(mockPrincipal))
        .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));

    verify(cartService, times(1)).getCartsForUser(eq("test@example.com"), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /users/me/carts with HTML should return cart fragment")
  void getCurrentCart_Html_ShouldReturnFragment() throws Exception {
    Set<Cart> carts = new HashSet<>();
    carts.add(mockCart);
    when(cartService.getAllCartsForUser("test@example.com")).thenReturn(carts);

    mockMvc.perform(get("/users/me/carts")
        .with(user(mockPrincipal))
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("fragments/cart :: cart-content"))
        .andExpect(model().attributeExists("carts"));

    verify(cartService, times(1)).getAllCartsForUser("test@example.com");
  }

  @Test
  @DisplayName("GET /users/me/carts with empty result should return empty page")
  void getCurrentCart_Api_EmptyResult() throws Exception {
    Page<Cart> emptyPage = Page.empty();
    when(cartService.getCartsForUser(eq("test@example.com"), any(Pageable.class))).thenReturn(emptyPage);

    mockMvc.perform(get("/users/me/carts")
        .with(user(mockPrincipal))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  // =============================
  // TESTS GET SPECIFIC CART
  // =============================

  @Test
  @DisplayName("GET /users/me/carts/{uuid} with JSON should return specific cart")
  void getSpecificCart_Api_ShouldReturnJson() throws Exception {
    when(cartService.getCartByUuidForUser("cart-123", "test@example.com")).thenReturn(mockCart);

    mockMvc.perform(get("/users/me/carts/cart-123")
        .with(user(mockPrincipal))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value("cart-123"))
        .andExpect(jsonPath("$.status").value("DRAFT"));

    verify(cartService, times(1)).getCartByUuidForUser("cart-123", "test@example.com");
  }

  @Test
  @DisplayName("GET /users/me/carts/{uuid} with XML should return specific cart")
  void getSpecificCart_Api_ShouldReturnXml() throws Exception {
    when(cartService.getCartByUuidForUser("cart-123", "test@example.com")).thenReturn(mockCart);

    mockMvc.perform(get("/users/me/carts/cart-123")
        .with(user(mockPrincipal))
        .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));

    verify(cartService, times(1)).getCartByUuidForUser("cart-123", "test@example.com");
  }

  // =============================
  // TESTS ADD ITEM TO CART
  // =============================

  @Test
  @DisplayName("POST /users/me/carts/items with JSON should add item and return cart")
  void addItemToCart_Api_ShouldReturnJson() throws Exception {
    CartItemPostRequest request = new CartItemPostRequest();
    request.setItem("item-456");

    when(cartService.addItemToUserCarts("item-456", "test@example.com")).thenReturn(mockCart);

    mockMvc.perform(post("/users/me/carts/items")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value("cart-123"));

    verify(cartService, times(1)).addItemToUserCarts("item-456", "test@example.com");
  }

  @Test
  @DisplayName("POST /users/me/carts/items with XML should add item and return cart")
  void addItemToCart_Api_ShouldReturnXml() throws Exception {
    CartItemPostRequest request = new CartItemPostRequest();
    request.setItem("item-456");

    when(cartService.addItemToUserCarts("item-456", "test@example.com")).thenReturn(mockCart);

    String xmlRequest = "<CartItemPostRequest><item>item-456</item></CartItemPostRequest>";

    mockMvc.perform(post("/users/me/carts/items")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_XML)
        .content(xmlRequest)
        .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));

    verify(cartService, times(1)).addItemToUserCarts("item-456", "test@example.com");
  }

  @Test
  @DisplayName("POST /users/me/carts/items with form should add item and return HTML")
  void addItemToCart_Html_ShouldReturnFragment() throws Exception {
    Set<Cart> carts = new HashSet<>();
    carts.add(mockCart);

    when(cartService.addItemToUserCarts("00000000-0000-0000-0000-000000000123", "test@example.com"))
        .thenReturn(mockCart);
    when(cartService.getAllCartsForUser("test@example.com")).thenReturn(carts);

    mockMvc.perform(post("/users/me/carts/items")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("item", "00000000-0000-0000-0000-000000000123"))
        .andExpect(status().isOk())
        .andExpect(view().name("fragments/cart :: cart-content"))
        .andExpect(model().attributeExists("bundles"));

    verify(cartService, times(1)).addItemToUserCarts("00000000-0000-0000-0000-000000000123", "test@example.com");
    verify(cartService, times(1)).getAllCartsForUser("test@example.com");
  }

  @Test
  @DisplayName("POST /users/me/carts/items without CSRF should fail")
  void addItemToCart_WithoutCsrf_ShouldFail() throws Exception {
    CartItemPostRequest request = new CartItemPostRequest();
    request.setItem("item-456");

    mockMvc.perform(post("/users/me/carts/items")
        .with(user(mockPrincipal))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  // =============================
  // TESTS REMOVE ITEM FROM CART
  // =============================

  @Test
  @DisplayName("DELETE /users/me/carts/items/{itemUuid} should remove item and return HTML")
  void removeItemFromCart_Html_ShouldReturnFragment() throws Exception {
    Set<Cart> carts = new HashSet<>();
    carts.add(mockCart);

    when(cartService.removeItemFromUserCart("item-456", "test@example.com")).thenReturn(mockCart);
    when(cartService.getAllCartsForUser("test@example.com")).thenReturn(carts);

    mockMvc.perform(delete("/users/me/carts/items/item-456")
        .with(user(mockPrincipal))
        .with(csrf())
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("fragments/cart :: cart-content"))
        .andExpect(model().attributeExists("bundles"));

    verify(cartService, times(1)).removeItemFromUserCart("item-456", "test@example.com");
    verify(cartService, times(1)).getAllCartsForUser("test@example.com");
  }

  @Test
  @DisplayName("DELETE /users/me/carts/items/{itemUuid} without CSRF should fail")
  void removeItemFromCart_WithoutCsrf_ShouldFail() throws Exception {
    mockMvc.perform(delete("/users/me/carts/items/item-456")
        .with(user(mockPrincipal))
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isForbidden());
  }

  // =============================
  // TESTS UPDATE CART STATUS
  // =============================

  @Test
  @DisplayName("PATCH /users/me/carts/{uuid} with JSON should update status")
  void updateStatus_Api_ShouldReturnJson() throws Exception {
    CartPatchRequest request = new CartPatchRequest();
    request.setStatus(CartStatus.SENT);

    mockCart.setStatus(CartStatus.SENT);
    when(cartService.changeStatus("cart-123", CartStatus.SENT, "test@example.com")).thenReturn(mockCart);

    mockMvc.perform(patch("/users/me/carts/cart-123")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value("cart-123"))
        .andExpect(jsonPath("$.status").value("SENT"));

    verify(cartService, times(1)).changeStatus("cart-123", CartStatus.SENT, "test@example.com");
  }

  @Test
  @DisplayName("PATCH /users/me/carts/{uuid} with XML should update status")
  void updateStatus_Api_ShouldReturnXml() throws Exception {
    CartPatchRequest request = new CartPatchRequest();
    request.setStatus(CartStatus.SENT);

    mockCart.setStatus(CartStatus.SENT);
    when(cartService.changeStatus("cart-123", CartStatus.SENT, "test@example.com")).thenReturn(mockCart);

    String xmlRequest = "<CartPatchRequest><status>SENT</status></CartPatchRequest>";

    mockMvc.perform(patch("/users/me/carts/cart-123")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_XML)
        .content(xmlRequest)
        .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));

    verify(cartService, times(1)).changeStatus("cart-123", CartStatus.SENT, "test@example.com");
  }

  @Test
  @DisplayName("PATCH /users/me/carts/{uuid} with form should update and return profile")
  void updateStatus_Html_ShouldReturnProfile() throws Exception {
    mockCart.setStatus(CartStatus.SENT);
    when(cartService.changeStatus("cart-123", CartStatus.SENT, "test@example.com")).thenReturn(mockCart);
    when(userService.getByEmail("test@example.com")).thenReturn(mockUser);

    mockMvc.perform(patch("/users/me/carts/cart-123")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("status", "SENT"))
        .andExpect(status().isOk())
        .andExpect(view().name("users/me"))
        .andExpect(model().attributeExists("user"))
        .andExpect(model().attribute("user", mockUser));

    verify(cartService, times(1)).changeStatus("cart-123", CartStatus.SENT, "test@example.com");
    verify(userService, times(1)).getByEmail("test@example.com");
  }

  @Test
  @DisplayName("PATCH /users/me/carts/{uuid} without CSRF should fail")
  void updateStatus_WithoutCsrf_ShouldFail() throws Exception {
    CartPatchRequest request = new CartPatchRequest();
    request.setStatus(CartStatus.SENT);

    mockMvc.perform(patch("/users/me/carts/cart-123")
        .with(user(mockPrincipal))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PATCH /users/me/carts/{uuid} with invalid status should fail validation")
  void updateStatus_InvalidStatus_ShouldFailValidation() throws Exception {
    mockMvc.perform(patch("/users/me/carts/cart-123")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"status\": \"INVALID_STATUS\"}"))
        .andExpect(status().isBadRequest());
  }

  // =============================
  // TESTS AUTHENTICATION
  // =============================

  @Test
  @DisplayName("GET /users/me/carts without authentication should fail")
  void getCurrentCart_WithoutAuth_ShouldFail() throws Exception {
    mockMvc.perform(get("/users/me/carts")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /users/me/carts/items without authentication should fail")
  void addItemToCart_WithoutAuth_ShouldFail() throws Exception {
    CartItemPostRequest request = new CartItemPostRequest();
    request.setItem("item-456");

    mockMvc.perform(post("/users/me/carts/items")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  // =============================
  // TESTS PAGINATION
  // =============================

  @Test
  @DisplayName("GET /users/me/carts with pagination parameters should pass them to service")
  void getCurrentCart_WithPagination_ShouldUseParameters() throws Exception {
    Page<Cart> page = new PageImpl<>(Collections.singletonList(mockCart));
    when(cartService.getCartsForUser(eq("test@example.com"), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/users/me/carts")
        .with(user(mockPrincipal))
        .param("page", "2")
        .param("size", "20")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());

    verify(cartService, times(1)).getCartsForUser(eq("test@example.com"), any(Pageable.class));
  }
}
