package com.regifted.app.search;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
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
import com.regifted.app.search.dto.SearchPostRequest;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User; // Ajustez selon votre package User

@WebMvcTest(SearchController.class)
class SearchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SearchService searchService;

  @Autowired
  private ObjectMapper objectMapper;

  private CustomUserPrincipal mockPrincipal;
  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setUuid("ddd");

    mockPrincipal = new CustomUserPrincipal(mockUser);
  }

  // =============================
  // TESTS CREATE SEARCH
  // =============================

  @Test
  void createSearchHtmx_ShouldReturnHtmlButton() throws Exception {
    Search mockSearch = new Search();
    when(searchService.createSearch(anyString(), any())).thenReturn(mockSearch);

    mockMvc.perform(post("/users/me/searches")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("query", "iPhone 15"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Saved !")))
        .andExpect(content().string(containsString("disabled")));
  }

  @Test
  void createSearchApi_ShouldReturnCreatedStatus() throws Exception {
    SearchPostRequest req = new SearchPostRequest();
    req.setQuery("Lego Technic");

    Search createdSearch = new Search();
    createdSearch.setUuid("uuid-123");
    createdSearch.setQuery("Lego Technic");

    when(searchService.createSearch(eq("Lego Technic"), any())).thenReturn(createdSearch);

    mockMvc.perform(post("/users/me/searches")
        .with(user(mockPrincipal))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/users/me/searches/uuid-123"))
        .andExpect(jsonPath("$.uuid").value("uuid-123"));
  }

  // =============================
  // TESTS GET SEARCHES
  // =============================

  @Test
  void getSearchesApi_ShouldReturnPagedJson() throws Exception {
    Page<Search> page = new PageImpl<>(Collections.emptyList());
    when(searchService.getAllSearchesForUser(any(), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/users/me/searches")
        .with(user(mockPrincipal))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  /*
   * @Test
   * void getSearchesHtml_ShouldReturnListView() throws Exception {
   * Page<Search> page = new PageImpl<>(Collections.emptyList());
   * when(searchService.getAllSearchesForUser(any(),
   * any(Pageable.class))).thenReturn(page);
   * 
   * mockMvc.perform(get("/searches")
   * .with(user(mockPrincipal))
   * .accept(MediaType.TEXT_HTML))
   * .andExpect(status().isOk())
   * .andExpect(view().name("searches/list"))
   * .andExpect(model().attributeExists("searches"));
   * }
   */

  // =============================
  // TESTS GET BY UUID
  // =============================

  @Test
  void getSearchByUuid_ShouldRedirectToItemsWithQuery() throws Exception {
    Search search = new Search();
    search.setUuid("abc-456");
    search.setQuery("Test Query");

    when(searchService.getSearchByUuid(eq("abc-456"), any())).thenReturn(search);

    mockMvc.perform(get("/users/me/searches/abc-456")
        .with(user(mockPrincipal)))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "/items?q=Test%20Query"));
  }

  // =============================
  // TESTS DELETE
  // =============================

  @Test
  void deleteSearchApi_ShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/users/me/searches/abc-456")
        .with(user(mockPrincipal))
        .with(csrf()))
        .andExpect(status().isNoContent());

    verify(searchService, times(1)).deleteSearch(eq("abc-456"), any());
  }
}
