package com.regifted.app.keyword;

import com.regifted.app.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KeywordService Unit Tests")
class KeywordServiceTest {

  @Mock
  private KeywordRepository keywordRepository;

  @InjectMocks
  private KeywordService keywordService;

  private Keyword createKeyword(String uuid, String name) {
    Keyword keyword = new Keyword(
        uuid,
        name,
        Set.of());
    return keyword;
  }

  private Page<Keyword> createPage(List<Keyword> keywords, Pageable pageable) {
    return new PageImpl<>(keywords, pageable, keywords.size());
  }

  // =============================
  // GET ALL KEYWORDS TESTS
  // =============================

  @Test
  @DisplayName("Should return paginated keywords")
  void testGetAllKeywords_ReturnsPaginatedResults() {
    // Given
    Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "antique"),
        createKeyword("uuid-2", "vintage"),
        createKeyword("uuid-3", "retro"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findAll(pageable)).thenReturn(page);

    // When
    Page<Keyword> result = keywordService.getAllKeywords(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent()).containsExactlyElementsOf(keywords);
  }

  @Test
  @DisplayName("Should call repository with correct pageable")
  void testGetAllKeywords_CallsRepositoryWithPageable() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    Page<Keyword> emptyPage = Page.empty(pageable);
    when(keywordRepository.findAll(pageable)).thenReturn(emptyPage);

    // When
    keywordService.getAllKeywords(pageable);

    // Then
    verify(keywordRepository, times(1)).findAll(pageable);
  }

  @Test
  @DisplayName("Should handle empty result")
  void testGetAllKeywords_HandlesEmptyResult() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    Page<Keyword> emptyPage = Page.empty(pageable);
    when(keywordRepository.findAll(pageable)).thenReturn(emptyPage);

    // When
    Page<Keyword> result = keywordService.getAllKeywords(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  // =============================
  // SEARCH KEYWORDS TESTS
  // =============================

  // Basic Search Tests

  @Test
  @DisplayName("Should find keywords containing query (case-insensitive)")
  void testSearchKeywords_FindsMatchingKeywords() {
    // Given
    String query = "fur";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "furniture"),
        createKeyword("uuid-2", "furry"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("fur", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).extracting(Keyword::getName)
        .containsExactly("furniture", "furry");
  }

  @Test
  @DisplayName("Should return paginated search results")
  void testSearchKeywords_ReturnsPaginatedResults() {
    // Given
    String query = "vintage";
    Pageable pageable = PageRequest.of(0, 10);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "vintage"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("vintage", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Should call repository with normalized query")
  void testSearchKeywords_CallsRepositoryWithNormalizedQuery() {
    // Given
    String query = "  VINTAGE  ";
    String normalized = "vintage";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Keyword> emptyPage = Page.empty(pageable);
    when(keywordRepository.findByNameContainingIgnoreCase(normalized, pageable))
        .thenReturn(emptyPage);

    // When
    keywordService.searchKeywords(query, pageable);

    // Then
    verify(keywordRepository, times(1))
        .findByNameContainingIgnoreCase(eq(normalized), eq(pageable));
  }

  @Test
  @DisplayName("Should return empty page when no matches")
  void testSearchKeywords_NoMatches() {
    // Given
    String query = "nonexistent";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Keyword> emptyPage = Page.empty(pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("nonexistent", pageable))
        .thenReturn(emptyPage);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).isEmpty();
  }

  // Query Normalization Tests

  @Test
  @DisplayName("Should trim whitespace from query")
  void testSearchKeywords_TrimsWhitespace() {
    // Given
    String query = "  vintage  ";
    String trimmed = "vintage";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Keyword> page = Page.empty(pageable);
    when(keywordRepository.findByNameContainingIgnoreCase(trimmed, pageable))
        .thenReturn(page);

    // When
    keywordService.searchKeywords(query, pageable);

    // Then
    verify(keywordRepository).findByNameContainingIgnoreCase(eq(trimmed), any());
  }

  @Test
  @DisplayName("Should convert query to lowercase")
  void testSearchKeywords_ConvertsToLowercase() {
    // Given
    String query = "VINTAGE";
    String lowercase = "vintage";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Keyword> page = Page.empty(pageable);
    when(keywordRepository.findByNameContainingIgnoreCase(lowercase, pageable))
        .thenReturn(page);

    // When
    keywordService.searchKeywords(query, pageable);

    // Then
    verify(keywordRepository).findByNameContainingIgnoreCase(eq(lowercase), any());
  }

  @Test
  @DisplayName("Should handle mixed case query")
  void testSearchKeywords_MixedCase() {
    // Given
    String query = "ViNtAgE";
    String normalized = "vintage";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "vintage"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase(normalized, pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
    verify(keywordRepository).findByNameContainingIgnoreCase(eq(normalized), any());
  }

  @Test
  @DisplayName("Should normalize query before search")
  void testSearchKeywords_NormalizesQuery() {
    // Given
    String query = "  VINTAGE  ";
    String normalized = "vintage";
    Pageable pageable = PageRequest.of(0, 20);
    Page<Keyword> page = Page.empty(pageable);
    when(keywordRepository.findByNameContainingIgnoreCase(normalized, pageable))
        .thenReturn(page);

    // When
    keywordService.searchKeywords(query, pageable);

    // Then
    ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
    verify(keywordRepository).findByNameContainingIgnoreCase(queryCaptor.capture(), any());
    assertThat(queryCaptor.getValue()).isEqualTo(normalized);
  }

  // Edge Cases Tests

  @Test
  @DisplayName("Should return all keywords when query is empty string")
  void testSearchKeywords_EmptyQuery() {
    // Given
    String query = "";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> allKeywords = Arrays.asList(
        createKeyword("uuid-1", "vintage"),
        createKeyword("uuid-2", "retro"));
    Page<Keyword> page = createPage(allKeywords, pageable);
    when(keywordRepository.findAll(pageable)).thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(2);
    verify(keywordRepository).findAll(pageable);
    verify(keywordRepository, never()).findByNameContainingIgnoreCase(any(), any());
  }

  @Test
  @DisplayName("Should return all keywords when query is blank")
  void testSearchKeywords_BlankQuery() {
    // Given
    String query = "   ";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> allKeywords = Collections.singletonList(createKeyword("uuid-1", "vintage"));
    Page<Keyword> page = createPage(allKeywords, pageable);
    when(keywordRepository.findAll(pageable)).thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
    verify(keywordRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should handle query with special characters")
  void testSearchKeywords_WithSpecialCharacters() {
    // Given
    String query = "art&craft";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "art&craft"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("art&craft", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Should handle query with accents")
  void testSearchKeywords_WithAccents() {
    // Given
    String query = "café";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "café"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("café", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Should handle single character query")
  void testSearchKeywords_SingleCharacter() {
    // Given
    String query = "a";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "antique"),
        createKeyword("uuid-2", "art"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("a", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("Should find partial matches")
  void testSearchKeywords_PartialMatches() {
    // Given
    String query = "fur";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "furniture"),
        createKeyword("uuid-2", "furry"),
        createKeyword("uuid-3", "sulfur"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("fur", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  @DisplayName("Should find keywords at start of name")
  void testSearchKeywords_MatchesAtStart() {
    // Given
    String query = "vin";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "vintage"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("vin", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).startsWith("vin");
  }

  @Test
  @DisplayName("Should find keywords at end of name")
  void testSearchKeywords_MatchesAtEnd() {
    // Given
    String query = "age";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "vintage"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("age", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).endsWith("age");
  }

  @Test
  @DisplayName("Should find keywords in middle of name")
  void testSearchKeywords_MatchesInMiddle() {
    // Given
    String query = "nta";
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "vintage"));
    Page<Keyword> page = createPage(keywords, pageable);
    when(keywordRepository.findByNameContainingIgnoreCase("nta", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result = keywordService.searchKeywords(query, pageable);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).contains("nta");
  }

  @Test
  @DisplayName("Should not be case sensitive in search")
  void testSearchKeywords_CaseInsensitive() {
    // Given
    Pageable pageable = PageRequest.of(0, 20);
    List<Keyword> keywords = Collections.singletonList(createKeyword("uuid-1", "vintage"));
    Page<Keyword> page = createPage(keywords, pageable);

    when(keywordRepository.findByNameContainingIgnoreCase("vintage", pageable))
        .thenReturn(page);

    // When
    Page<Keyword> result1 = keywordService.searchKeywords("VINTAGE", pageable);
    Page<Keyword> result2 = keywordService.searchKeywords("vintage", pageable);
    Page<Keyword> result3 = keywordService.searchKeywords("ViNtAgE", pageable);

    // Then
    assertThat(result1.getContent()).hasSize(1);
    assertThat(result2.getContent()).hasSize(1);
    assertThat(result3.getContent()).hasSize(1);
  }

  // =============================
  // GET BY ID TESTS
  // =============================

  // Success Cases

  @Test
  @DisplayName("Should return keyword when found by UUID")
  void testGetById_Success() {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordRepository.findByUuid(uuid)).thenReturn(Optional.of(keyword));

    // When
    Keyword result = keywordService.getByUuid(uuid);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isEqualTo(uuid);
    assertThat(result.getName()).isEqualTo("vintage");
  }

  @Test
  @DisplayName("Should call repository findByUuid")
  void testGetById_CallsRepository() {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    Keyword keyword = createKeyword(uuid, "vintage");
    when(keywordRepository.findByUuid(uuid)).thenReturn(Optional.of(keyword));

    // When
    keywordService.getByUuid(uuid);

    // Then
    verify(keywordRepository, times(1)).findByUuid(uuid);
  }

  @Test
  @DisplayName("Should return correct keyword data")
  void testGetById_ReturnsCorrectData() {
    // Given
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    Keyword keyword = createKeyword(uuid, "vintage-camera");
    when(keywordRepository.findByUuid(uuid)).thenReturn(Optional.of(keyword));

    // When
    Keyword result = keywordService.getByUuid(uuid);

    // Then
    assertThat(result.getUuid()).isEqualTo(uuid);
    assertThat(result.getName()).isEqualTo("vintage-camera");
  }

  // Error Cases

  @Test
  @DisplayName("Should throw NotFoundException when keyword not found")
  void testGetById_ThrowsNotFoundException() {
    // Given
    String uuid = "999e8400-e29b-41d4-a716-446655440999";
    when(keywordRepository.findByUuid(uuid)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> keywordService.getByUuid(uuid))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(uuid);
  }

  @Test
  @DisplayName("Should throw NotFoundException with correct message")
  void testGetById_NotFoundExceptionMessage() {
    // Given
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    when(keywordRepository.findByUuid(uuid)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> keywordService.getByUuid(uuid))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Not found with id: " + uuid);
  }

  @Test
  @DisplayName("Should not return null")
  void testGetById_NeverReturnsNull() {
    // Given
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    when(keywordRepository.findByUuid(uuid)).thenReturn(Optional.empty());

    // When & Then - should throw, not return null
    assertThatThrownBy(() -> keywordService.getByUuid(uuid))
        .isInstanceOf(NotFoundException.class);
  }

  // Edge Cases

  @Test
  @DisplayName("Should handle null UUID")
  void testGetById_WithNullId() {
    // Given
    String uuid = null;

    // When & Then
    assertThatThrownBy(() -> keywordService.getByUuid(uuid))
        .isInstanceOf(NotFoundException.class);
  }
}
