package com.regifted.app.keyword;

import com.regifted.app.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = KeywordController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("KeywordController Unit Tests")
class KeywordControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private KeywordService keywordService;

  private Keyword createKeyword(String uuid, String name) {
    return new Keyword(uuid, name, Set.of());
  }

  private Page<Keyword> createPage(List<Keyword> keywords, Pageable pageable) {
    return new PageImpl<>(keywords, pageable, keywords.size());
  }

  // =============================
  // GET /keywords - LIST/SEARCH TESTS
  // =============================

  // Basic Functionality Tests

  @Test
  @DisplayName("GET /keywords - Should return 200 OK with paginated keywords when no query provided")
  void testGetKeywords_NoQuery_ReturnsOk() throws Exception {
    // Given
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "antique"),
        createKeyword("uuid-2", "vintage"));
    Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(page);

    // When & Then
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2));
  }

  @Test
  @DisplayName("GET /keywords - Should return 200 OK with search results when query provided")
  void testGetKeywords_WithQuery_ReturnsSearchResults() throws Exception {
    // Given
    String query = "vintage";
    List<Keyword> keywords = Collections.singletonList(
        createKeyword("uuid-1", "vintage"));
    Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordService.searchKeywords(eq(query), any(Pageable.class))).thenReturn(page);

    // When & Then
    mockMvc.perform(get("/keywords")
        .param("q", query))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("vintage"));
  }

  @Test
  @DisplayName("GET /keywords - Should call getAllKeywords service method when no query")
  void testGetKeywords_NoQuery_CallsGetAllKeywords() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService, times(1)).getAllKeywords(any(Pageable.class));
    verify(keywordService, never()).searchKeywords(any(), any());
  }

  @Test
  @DisplayName("GET /keywords - Should call searchKeywords service method when query exists")
  void testGetKeywords_WithQuery_CallsSearchKeywords() throws Exception {
    // Given
    String query = "vintage";
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.searchKeywords(eq(query), any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", query))
        .andExpect(status().isOk());

    // Then
    verify(keywordService, times(1)).searchKeywords(eq(query), any(Pageable.class));
    verify(keywordService, never()).getAllKeywords(any());
  }

  @Test
  @DisplayName("GET /keywords - Should return correct content type")
  void testGetKeywords_ReturnsJsonContentType() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  // Pagination Tests

  @Test
  @DisplayName("GET /keywords - Should use default page size of 20 when not specified")
  void testGetKeywords_DefaultPageSize() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).getAllKeywords(argThat(pageable -> pageable.getPageSize() == 20));
  }

  @Test
  @DisplayName("GET /keywords - Should use default sort by name when not specified")
  void testGetKeywords_DefaultSort() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).getAllKeywords(argThat(pageable -> pageable.getSort().getOrderFor("name") != null));
  }

  @Test
  @DisplayName("GET /keywords - Should respect custom page size parameter")
  void testGetKeywords_CustomPageSize() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("size", "10"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).getAllKeywords(argThat(pageable -> pageable.getPageSize() == 10));
  }

  @Test
  @DisplayName("GET /keywords - Should respect custom page number parameter")
  void testGetKeywords_CustomPageNumber() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("page", "2"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).getAllKeywords(argThat(pageable -> pageable.getPageNumber() == 2));
  }

  @Test
  @DisplayName("GET /keywords - Should respect custom sort parameter")
  void testGetKeywords_CustomSort() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("sort", "name,desc"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).getAllKeywords(argThat(pageable -> {
      Sort.Order order = pageable.getSort().getOrderFor("name");
      return order != null && order.getDirection() == Sort.Direction.DESC;
    }));
  }

  @Test
  @DisplayName("GET /keywords - Should handle multiple sort parameters")
  void testGetKeywords_MultipleSortParameters() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("sort", "name,desc")
        .param("sort", "uuid,asc"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).getAllKeywords(argThat(pageable -> pageable.getSort().getOrderFor("name") != null &&
        pageable.getSort().getOrderFor("uuid") != null));
  }

  // Query Parameter Handling Tests

  @Test
  @DisplayName("GET /keywords - Should treat empty string query as no query")
  void testGetKeywords_EmptyQuery_CallsGetAllKeywords() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", ""))
        .andExpect(status().isOk());

    // Then
    verify(keywordService, times(1)).getAllKeywords(any(Pageable.class));
    verify(keywordService, never()).searchKeywords(any(), any());
  }

  @Test
  @DisplayName("GET /keywords - Should treat whitespace-only query as no query")
  void testGetKeywords_WhitespaceQuery_CallsGetAllKeywords() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", "   "))
        .andExpect(status().isOk());

    // Then
    verify(keywordService, times(1)).getAllKeywords(any(Pageable.class));
    verify(keywordService, never()).searchKeywords(any(), any());
  }

  @Test
  @DisplayName("GET /keywords - Should pass query to service when provided")
  void testGetKeywords_PassesQueryToService() throws Exception {
    // Given
    String query = "vintage";
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.searchKeywords(eq(query), any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", query))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).searchKeywords(eq(query), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /keywords - Should handle query with special characters")
  void testGetKeywords_QueryWithSpecialCharacters() throws Exception {
    // Given
    String query = "art&craft";
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.searchKeywords(eq(query), any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", query))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).searchKeywords(eq(query), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /keywords - Should handle query with spaces")
  void testGetKeywords_QueryWithSpaces() throws Exception {
    // Given
    String query = "vintage furniture";
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.searchKeywords(eq(query), any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", query))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).searchKeywords(eq(query), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /keywords - Should pass query as-is to service (case-sensitive)")
  void testGetKeywords_QueryCaseSensitive() throws Exception {
    // Given
    String query = "ViNtAgE";
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.searchKeywords(eq(query), any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", query))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).searchKeywords(eq(query), any(Pageable.class));
  }

  // Response Structure Tests

  @Test
  @DisplayName("GET /keywords - Should return Page object with correct structure")
  void testGetKeywords_PageStructure() throws Exception {
    // Given
    List<Keyword> keywords = Collections.singletonList(
        createKeyword("uuid-1", "vintage"));
    Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(page);

    // When & Then
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.pageable").exists())
        .andExpect(jsonPath("$.totalElements").exists())
        .andExpect(jsonPath("$.totalPages").exists())
        .andExpect(jsonPath("$.size").exists())
        .andExpect(jsonPath("$.number").exists());
  }

  @Test
  @DisplayName("GET /keywords - Should return empty page when no results")
  void testGetKeywords_EmptyPage() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /keywords - Should include pagination metadata in response")
  void testGetKeywords_PaginationMetadata() throws Exception {
    // Given
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "vintage"),
        createKeyword("uuid-2", "retro"));
    Pageable pageable = PageRequest.of(1, 10, Sort.by("name"));
    Page<Keyword> page = new PageImpl<>(keywords, pageable, 25);
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(page);

    // When & Then
    mockMvc.perform(get("/keywords")
        .param("page", "1")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(25))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.number").value(1));
  }

  @Test
  @DisplayName("GET /keywords - Should return keywords in correct order")
  void testGetKeywords_CorrectOrder() throws Exception {
    // Given
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "antique"),
        createKeyword("uuid-2", "vintage"));
    Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(page);

    // When & Then
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("antique"))
        .andExpect(jsonPath("$.content[1].name").value("vintage"));
  }

  // Edge Cases Tests

  @Test
  @DisplayName("GET /keywords - Should handle null query parameter")
  void testGetKeywords_NullQuery() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService, times(1)).getAllKeywords(any(Pageable.class));
    verify(keywordService, never()).searchKeywords(any(), any());
  }

  @Test
  @DisplayName("GET /keywords - Should handle very long query string")
  void testGetKeywords_VeryLongQuery() throws Exception {
    // Given
    String longQuery = "a".repeat(1000);
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.searchKeywords(eq(longQuery), any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("q", longQuery))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).searchKeywords(eq(longQuery), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /keywords - Should handle page size of 0")
  void testGetKeywords_PageSizeZero() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When
    mockMvc.perform(get("/keywords")
        .param("size", "0"))
        .andExpect(status().isOk());

    // Then
    verify(keywordService).getAllKeywords(any(Pageable.class));
  }

  @Test
  @DisplayName("GET /keywords - Should handle negative page number")
  void testGetKeywords_NegativePageNumber() throws Exception {
    // Given
    Page<Keyword> emptyPage = Page.empty();
    when(keywordService.getAllKeywords(any(Pageable.class))).thenReturn(emptyPage);

    // When & Then
    mockMvc.perform(get("/keywords")
        .param("page", "-1"))
        .andExpect(status().isOk());
  }

  // =============================
  // GET /keywords/{uuid} TESTS
  // =============================

  // Basic Functionality Tests

  @Test
  @DisplayName("GET /keywords/{uuid} - Should return 200 OK with keyword when found")
  void testGetKeywordById_Found_ReturnsOk() throws Exception {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordService.getByUuid(uuid)).thenReturn(keyword);

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.uuid").value(uuid))
        .andExpect(jsonPath("$.name").value("vintage"));
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should call getByUuid service method with correct UUID")
  void testGetKeywordById_CallsServiceWithCorrectUuid() throws Exception {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordService.getByUuid(uuid)).thenReturn(keyword);

    // When
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isOk());

    // Then
    verify(keywordService, times(1)).getByUuid(eq(uuid));
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should return correct keyword data")
  void testGetKeywordById_ReturnsCorrectData() throws Exception {
    // Given
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    Keyword keyword = createKeyword(uuid, "vintage-camera");
    when(keywordService.getByUuid(uuid)).thenReturn(keyword);

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value(uuid))
        .andExpect(jsonPath("$.name").value("vintage-camera"));
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should return correct content type")
  void testGetKeywordById_ReturnsJsonContentType() throws Exception {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordService.getByUuid(uuid)).thenReturn(keyword);

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  // Error Cases Tests

  @Test
  @DisplayName("GET /keywords/{uuid} - Should return 404 when keyword not found")
  void testGetKeywordById_NotFound_Returns404() throws Exception {
    // Given
    String uuid = "999e8400-e29b-41d4-a716-446655440999";
    when(keywordService.getByUuid(uuid)).thenThrow(new NotFoundException(uuid));

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should return 404")
  void testGetKeywordById_NotFound_CorrectErrorMessage() throws Exception {
    // Given
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    when(keywordService.getByUuid(uuid)).thenThrow(new NotFoundException(uuid));

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should handle service throwing NotFoundException")
  void testGetKeywordById_ServiceThrowsNotFoundException() throws Exception {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    when(keywordService.getByUuid(uuid)).thenThrow(new NotFoundException(uuid));

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isNotFound());

    verify(keywordService, times(1)).getByUuid(uuid);
  }

  // UUID Validation Tests

  @Test
  @DisplayName("GET /keywords/{uuid} - Should accept valid UUID format")
  void testGetKeywordById_ValidUuidFormat() throws Exception {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordService.getByUuid(uuid)).thenReturn(keyword);

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").value(uuid));
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should handle invalid UUID format (let service handle)")
  void testGetKeywordById_InvalidUuidFormat() throws Exception {
    // Given
    String invalidUuid = "not-a-valid-uuid";
    when(keywordService.getByUuid(invalidUuid)).thenThrow(new NotFoundException(invalidUuid));

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", invalidUuid))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should handle empty string UUID")
  void testGetKeywordById_EmptyUuid() throws Exception {
    // When & Then
    mockMvc.perform(get("/keywords/"))
        .andExpect(status().isNotFound()); // 404 because path doesn't match
  }

  // Response Structure Tests

  @Test
  @DisplayName("GET /keywords/{uuid} - Should return keyword with all fields")
  void testGetKeywordById_AllFields() throws Exception {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordService.getByUuid(uuid)).thenReturn(keyword);

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.uuid").exists())
        .andExpect(jsonPath("$.name").exists());
  }

  @Test
  @DisplayName("GET /keywords/{uuid} - Should not return null body")
  void testGetKeywordById_NotNullBody() throws Exception {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordService.getByUuid(uuid)).thenReturn(keyword);

    // When & Then
    mockMvc.perform(get("/keywords/{uuid}", uuid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").exists())
        .andExpect(jsonPath("$").isNotEmpty());
  }
}
