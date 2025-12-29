package com.regifted.app.keyword;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KeywordService Unit Tests")
class KeywordServiceTest {

  @Mock
  private KeywordRepository keywordRepository;

  @InjectMocks
  private KeywordService keywordService;

  private Keyword createKeyword(String uuid, String name) {
    Keyword keyword = new Keyword();
    keyword.setUuid(uuid);
    keyword.setName(name);
    return keyword;
  }

  // =============================
  // GET MOST USED KEYWORDS TESTS
  // =============================

  @Test
  @DisplayName("Should return correct number of most used keywords")
  void testGetMostUsedKeywords_ReturnsCorrectNumber() {
    // Given
    int limit = 5;
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "vintage"),
        createKeyword("uuid-2", "retro"),
        createKeyword("uuid-3", "antique"),
        createKeyword("uuid-4", "classic"),
        createKeyword("uuid-5", "old"));

    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(keywords);

    // When
    List<Keyword> result = keywordService.getMostUsedKeywords(limit);

    // Then
    assertThat(result).hasSize(5);
    verify(keywordRepository, times(1)).findMostUsedKeywords(limit);
  }

  @Test
  @DisplayName("Should return keywords in correct order")
  void testGetMostUsedKeywords_ReturnsCorrectOrder() {
    // Given
    int limit = 3;
    Keyword vintage = createKeyword("uuid-1", "vintage");
    Keyword retro = createKeyword("uuid-2", "retro");
    Keyword antique = createKeyword("uuid-3", "antique");
    List<Keyword> keywords = Arrays.asList(vintage, retro, antique);

    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(keywords);

    // When
    List<Keyword> result = keywordService.getMostUsedKeywords(limit);

    // Then
    assertThat(result).containsExactly(vintage, retro, antique);
  }

  @Test
  @DisplayName("Should call repository with correct limit")
  void testGetMostUsedKeywords_CallsRepository() {
    // Given
    int limit = 10;
    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(Collections.emptyList());

    // When
    keywordService.getMostUsedKeywords(limit);

    // Then
    verify(keywordRepository, times(1)).findMostUsedKeywords(limit);
  }

  @Test
  @DisplayName("Should return empty list when no keywords exist")
  void testGetMostUsedKeywords_ReturnsEmptyList() {
    // Given
    int limit = 5;
    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(Collections.emptyList());

    // When
    List<Keyword> result = keywordService.getMostUsedKeywords(limit);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should handle zero limit")
  void testGetMostUsedKeywords_WithZeroLimit() {
    // Given
    int limit = 0;
    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(Collections.emptyList());

    // When
    List<Keyword> result = keywordService.getMostUsedKeywords(limit);

    // Then
    assertThat(result).isEmpty();
    verify(keywordRepository, times(1)).findMostUsedKeywords(0);
  }

  @Test
  @DisplayName("Should handle negative limit")
  void testGetMostUsedKeywords_WithNegativeLimit() {
    // Given
    int limit = -1;
    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(Collections.emptyList());

    // When
    List<Keyword> result = keywordService.getMostUsedKeywords(limit);

    // Then
    assertThat(result).isEmpty();
    verify(keywordRepository, times(1)).findMostUsedKeywords(-1);
  }

  @Test
  @DisplayName("Should handle large limit")
  void testGetMostUsedKeywords_WithLargeLimit() {
    // Given
    int limit = 1000;
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "vintage"),
        createKeyword("uuid-2", "retro"));
    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(keywords);

    // When
    List<Keyword> result = keywordService.getMostUsedKeywords(limit);

    // Then
    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("Should handle limit exceeding total keywords")
  void testGetMostUsedKeywords_LimitExceedsTotalKeywords() {
    // Given
    int limit = 100;
    List<Keyword> keywords = Arrays.asList(
        createKeyword("uuid-1", "vintage"),
        createKeyword("uuid-2", "retro"),
        createKeyword("uuid-3", "antique"));
    when(keywordRepository.findMostUsedKeywords(limit)).thenReturn(keywords);

    // When
    List<Keyword> result = keywordService.getMostUsedKeywords(limit);

    // Then
    assertThat(result).hasSize(3);
  }

  // =============================
  // GET BY NAME TESTS
  // =============================

  @Test
  @DisplayName("Should return keyword when found by name")
  void testGetByName_Success() {
    // Given
    String name = "vintage";
    Keyword keyword = createKeyword("uuid-1", name);
    when(keywordRepository.findByName(name)).thenReturn(Optional.of(keyword));

    // When
    Keyword result = keywordService.getByName(name);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(name);
    verify(keywordRepository, times(1)).findByName(name);
  }

  @Test
  @DisplayName("Should return null when keyword not found")
  void testGetByName_ReturnsNull() {
    // Given
    String name = "nonexistent";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());

    // When
    Keyword result = keywordService.getByName(name);

    // Then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should call repository findByName")
  void testGetByName_CallsRepository() {
    // Given
    String name = "vintage";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());

    // When
    keywordService.getByName(name);

    // Then
    verify(keywordRepository, times(1)).findByName(name);
  }

  @Test
  @DisplayName("Should find keyword with exact match")
  void testGetByName_ExactMatch() {
    // Given
    String name = "vintage";
    Keyword keyword = createKeyword("uuid-1", name);
    when(keywordRepository.findByName(name)).thenReturn(Optional.of(keyword));

    // When
    Keyword result = keywordService.getByName(name);

    // Then
    assertThat(result.getName()).isEqualTo(name);
  }

  @Test
  @DisplayName("Should handle null name in getByName")
  void testGetByName_WithNull() {
    // Given
    when(keywordRepository.findByName(null)).thenReturn(Optional.empty());

    // When
    Keyword result = keywordService.getByName(null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should handle empty string in getByName")
  void testGetByName_WithEmptyString() {
    // Given
    when(keywordRepository.findByName("")).thenReturn(Optional.empty());

    // When
    Keyword result = keywordService.getByName("");

    // Then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should handle blank string in getByName")
  void testGetByName_WithBlankString() {
    // Given
    String name = "   ";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());

    // When
    Keyword result = keywordService.getByName(name);

    // Then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should handle special characters in getByName")
  void testGetByName_WithSpecialCharacters() {
    // Given
    String name = "vintage&retro";
    Keyword keyword = createKeyword("uuid-1", name);
    when(keywordRepository.findByName(name)).thenReturn(Optional.of(keyword));

    // When
    Keyword result = keywordService.getByName(name);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(name);
  }

  // =============================
  // GET OR CREATE KEYWORD TESTS
  // =============================

  @Test
  @DisplayName("Should return existing keyword when found")
  void testGetOrCreateKeyword_ReturnsExisting() {
    // Given
    String name = "vintage";
    Keyword existingKeyword = createKeyword("uuid-1", name);
    when(keywordRepository.findByName(name)).thenReturn(Optional.of(existingKeyword));

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isEqualTo("uuid-1");
    assertThat(result.getName()).isEqualTo(name);
    verify(keywordRepository, times(1)).findByName(name);
    verify(keywordRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should not create duplicate when keyword exists")
  void testGetOrCreateKeyword_DoesNotCreateDuplicate() {
    // Given
    String name = "vintage";
    Keyword existingKeyword = createKeyword("uuid-1", name);
    when(keywordRepository.findByName(name)).thenReturn(Optional.of(existingKeyword));

    // When
    keywordService.getOrCreateKeyword(name);

    // Then
    verify(keywordRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should call findByName when getting or creating")
  void testGetOrCreateKeyword_CallsFindByName() {
    // Given
    String name = "vintage";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    keywordService.getOrCreateKeyword(name);

    // Then
    verify(keywordRepository, times(1)).findByName(name);
  }

  @Test
  @DisplayName("Should create new keyword when not found")
  void testGetOrCreateKeyword_CreatesNew() {
    // Given
    String name = "vintage";
    Keyword newKeyword = createKeyword("uuid-1", name);
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any(Keyword.class))).thenReturn(newKeyword);

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(name);
    verify(keywordRepository, times(1)).save(any(Keyword.class));
  }

  @Test
  @DisplayName("Should call save when creating new keyword")
  void testGetOrCreateKeyword_CallsSave() {
    // Given
    String name = "vintage";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    keywordService.getOrCreateKeyword(name);

    // Then
    ArgumentCaptor<Keyword> captor = ArgumentCaptor.forClass(Keyword.class);
    verify(keywordRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo(name);
  }

  @Test
  @DisplayName("Should save normalized name when creating")
  void testGetOrCreateKeyword_SavesNormalizedName() {
    // Given
    String input = "  VINTAGE  ";
    String normalized = "vintage";
    when(keywordRepository.findByName(normalized)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", normalized));

    // When
    keywordService.getOrCreateKeyword(input);

    // Then
    ArgumentCaptor<Keyword> captor = ArgumentCaptor.forClass(Keyword.class);
    verify(keywordRepository).save(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo(normalized);
  }

  // Normalization Logic Tests

  @Test
  @DisplayName("Should trim whitespace from keyword name")
  void testGetOrCreateKeyword_TrimsWhitespace() {
    // Given
    String input = "  vintage  ";
    String trimmed = "vintage";
    when(keywordRepository.findByName(trimmed)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", trimmed));

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    verify(keywordRepository).findByName(trimmed);
    assertThat(result.getName()).isEqualTo(trimmed);
  }

  @Test
  @DisplayName("Should convert to lowercase")
  void testGetOrCreateKeyword_ConvertsToLowercase() {
    // Given
    String input = "VINTAGE";
    String lowercase = "vintage";
    when(keywordRepository.findByName(lowercase)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", lowercase));

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    verify(keywordRepository).findByName(lowercase);
    assertThat(result.getName()).isEqualTo(lowercase);
  }

  @Test
  @DisplayName("Should trim and convert to lowercase")
  void testGetOrCreateKeyword_TrimAndLowercase() {
    // Given
    String input = "  VINTAGE  ";
    String normalized = "vintage";
    when(keywordRepository.findByName(normalized)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", normalized));

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    verify(keywordRepository).findByName(normalized);
    assertThat(result.getName()).isEqualTo(normalized);
  }

  @Test
  @DisplayName("Should preserve hyphens in keyword name")
  void testGetOrCreateKeyword_PreservesHyphens() {
    // Given
    String name = "art-deco";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result.getName()).isEqualTo(name);
  }

  @Test
  @DisplayName("Should preserve numbers in keyword name")
  void testGetOrCreateKeyword_PreservesNumbers() {
    // Given
    String name = "1980s";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result.getName()).isEqualTo(name);
  }

  // Null and Empty String Tests

  @Test
  @DisplayName("Should document null check order bug - throws NPE")
  void testGetOrCreateKeyword_NullCheckAfterTrim_Bug() {
    // This test documents the bug: name.trim() is called before null check
    // Given
    String input = null;

    // When & Then
    assertThatThrownBy(() -> keywordService.getOrCreateKeyword(input))
        .isInstanceOf(NullPointerException.class);

    // BUG: The service should check null BEFORE calling trim()
    // Expected behavior: return null without throwing exception
  }

  @Test
  @DisplayName("Should return null for empty string after trim")
  void testGetOrCreateKeyword_WithEmptyString_ReturnsNull() {
    // Given
    String input = "";

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    assertThat(result).isNull();
    verify(keywordRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should return null for blank string after trim")
  void testGetOrCreateKeyword_WithBlankString_ReturnsNull() {
    // Given
    String input = "   ";

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    assertThat(result).isNull();
    verify(keywordRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should not save when input is empty")
  void testGetOrCreateKeyword_DoesNotSaveWhenEmpty() {
    // Given
    String input = "";

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    assertThat(result).isNull();
    verify(keywordRepository, never()).save(any());
  }

  // Case Sensitivity Matching Tests

  @Test
  @DisplayName("Should find existing keyword case insensitive")
  void testGetOrCreateKeyword_FindsExistingCaseInsensitive() {
    // Given
    String input = "VINTAGE";
    String normalized = "vintage";
    Keyword existingKeyword = createKeyword("uuid-1", normalized);
    when(keywordRepository.findByName(normalized)).thenReturn(Optional.of(existingKeyword));

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isEqualTo("uuid-1");
    verify(keywordRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should find existing keyword with whitespace")
  void testGetOrCreateKeyword_FindsExistingWithWhitespace() {
    // Given
    String input = "  vintage  ";
    String normalized = "vintage";
    Keyword existingKeyword = createKeyword("uuid-1", normalized);
    when(keywordRepository.findByName(normalized)).thenReturn(Optional.of(existingKeyword));

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isEqualTo("uuid-1");
    verify(keywordRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should find existing keyword with mixed case")
  void testGetOrCreateKeyword_MixedCase() {
    // Given
    String input = "ViNtAgE";
    String normalized = "vintage";
    Keyword existingKeyword = createKeyword("uuid-1", normalized);
    when(keywordRepository.findByName(normalized)).thenReturn(Optional.of(existingKeyword));

    // When
    Keyword result = keywordService.getOrCreateKeyword(input);

    // Then
    assertThat(result).isNotNull();
    verify(keywordRepository, never()).save(any());
  }

  // Special Characters Tests

  @Test
  @DisplayName("Should handle special characters")
  void testGetOrCreateKeyword_WithSpecialCharacters() {
    // Given
    String name = "art&craft";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result.getName()).isEqualTo(name);
  }

  @Test
  @DisplayName("Should handle accents")
  void testGetOrCreateKeyword_WithAccents() {
    // Given
    String name = "café";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result.getName()).isEqualTo(name);
  }

  @Test
  @DisplayName("Should handle Unicode characters")
  void testGetOrCreateKeyword_WithUnicode() {
    // Given
    String name = "カメラ";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result.getName()).isEqualTo(name);
  }

  @Test
  @DisplayName("Should handle emoji")
  void testGetOrCreateKeyword_WithEmoji() {
    // Given
    String name = "vintage📷";
    when(keywordRepository.findByName(name)).thenReturn(Optional.empty());
    when(keywordRepository.save(any())).thenReturn(createKeyword("uuid-1", name));

    // When
    Keyword result = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(result.getName()).isEqualTo(name);
  }

  // Business Logic Tests

  @Test
  @DisplayName("Should be idempotent - calling twice with same name")
  void testGetOrCreateKeyword_IdempotentOperation() {
    // Given
    String name = "vintage";
    Keyword keyword = createKeyword("uuid-1", name);
    when(keywordRepository.findByName(name))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(keyword));
    when(keywordRepository.save(any())).thenReturn(keyword);

    // When
    Keyword first = keywordService.getOrCreateKeyword(name);
    Keyword second = keywordService.getOrCreateKeyword(name);

    // Then
    assertThat(first.getName()).isEqualTo(second.getName());
    verify(keywordRepository, times(1)).save(any()); // Only saved once
  }

  @Test
  @DisplayName("Should create different keywords for different names")
  void testGetOrCreateKeyword_DifferentNamesCreateDifferent() {
    // Given
    String name1 = "vintage";
    String name2 = "retro";
    Keyword keyword1 = createKeyword("uuid-1", name1);
    Keyword keyword2 = createKeyword("uuid-2", name2);

    when(keywordRepository.findByName(name1)).thenReturn(Optional.empty());
    when(keywordRepository.findByName(name2)).thenReturn(Optional.empty());
    when(keywordRepository.save(any()))
        .thenReturn(keyword1)
        .thenReturn(keyword2);

    // When
    Keyword result1 = keywordService.getOrCreateKeyword(name1);
    Keyword result2 = keywordService.getOrCreateKeyword(name2);

    // Then
    assertThat(result1.getName()).isEqualTo(name1);
    assertThat(result2.getName()).isEqualTo(name2);
    assertThat(result1.getUuid()).isNotEqualTo(result2.getUuid());
  }

  @Test
  @DisplayName("Should handle multiple whitespace variations consistently")
  void testGetOrCreateKeyword_MultipleWhitespaceVariations() {
    // Given
    String normalized = "vintage";
    Keyword keyword = createKeyword("uuid-1", normalized);
    when(keywordRepository.findByName(normalized))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(keyword))
        .thenReturn(Optional.of(keyword))
        .thenReturn(Optional.of(keyword));
    when(keywordRepository.save(any())).thenReturn(keyword);

    // When
    Keyword result1 = keywordService.getOrCreateKeyword("vintage");
    Keyword result2 = keywordService.getOrCreateKeyword(" vintage");
    Keyword result3 = keywordService.getOrCreateKeyword("vintage ");
    Keyword result4 = keywordService.getOrCreateKeyword("  vintage  ");

    // Then
    assertThat(result1.getName()).isEqualTo(normalized);
    assertThat(result2.getName()).isEqualTo(normalized);
    assertThat(result3.getName()).isEqualTo(normalized);
    assertThat(result4.getName()).isEqualTo(normalized);
  }

}
