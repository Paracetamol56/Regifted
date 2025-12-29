package com.regifted.app.search;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService Unit Tests")
class SearchServiceTest {

  @Mock
  private SearchRepository searchRepository;

  @InjectMocks
  private SearchService searchService;

  private User testUser;
  private User otherUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setUuid("user-123");
    testUser.setEmail("test@example.com");

    otherUser = new User();
    otherUser.setUuid("user-456");
    otherUser.setEmail("other@example.com");
  }

  private Search createSearch(String uuid, String query, User user) {
    Search search = new Search();
    search.setUuid(uuid);
    search.setQuery(query);
    search.setUser(user);
    return search;
  }

  // =============================
  // CREATE SEARCH TESTS
  // =============================

  @Test
  @DisplayName("Should create search with correct query and user")
  void testCreateSearch_Success() {
    // Given
    String query = "vintage camera";
    Search savedSearch = new Search();
    savedSearch.setUuid("search-uuid-123");
    savedSearch.setQuery(query);
    savedSearch.setUser(testUser);

    when(searchRepository.save(any(Search.class))).thenReturn(savedSearch);

    // When
    Search result = searchService.createSearch(query, testUser);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getQuery()).isEqualTo(query);
    assertThat(result.getUser()).isEqualTo(testUser);
    verify(searchRepository, times(1)).save(any(Search.class));
  }

  @Test
  @DisplayName("Should generate UUID when creating search")
  void testCreateSearch_GeneratesUUID() {
    // Given
    String query = "vintage camera";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search result = searchService.createSearch(query, testUser);

    // Then
    assertThat(result.getUuid()).isNotNull();
    assertThat(result.getUuid()).isNotEmpty();
  }

  @Test
  @DisplayName("Should generate unique UUIDs for each search")
  void testCreateSearch_UUIDIsUnique() {
    // Given
    String query = "vintage camera";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search search1 = searchService.createSearch(query, testUser);
    Search search2 = searchService.createSearch(query, testUser);

    // Then
    assertThat(search1.getUuid()).isNotEqualTo(search2.getUuid());
  }

  @Test
  @DisplayName("Should call repository save once")
  void testCreateSearch_CallsRepositorySave() {
    // Given
    String query = "vintage camera";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    searchService.createSearch(query, testUser);

    // Then
    ArgumentCaptor<Search> searchCaptor = ArgumentCaptor.forClass(Search.class);
    verify(searchRepository, times(1)).save(searchCaptor.capture());

    Search capturedSearch = searchCaptor.getValue();
    assertThat(capturedSearch.getQuery()).isEqualTo(query);
    assertThat(capturedSearch.getUser()).isEqualTo(testUser);
    assertThat(capturedSearch.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("Should return persisted search from repository")
  void testCreateSearch_ReturnsPersistedSearch() {
    // Given
    String query = "vintage camera";
    Search persistedSearch = new Search();
    persistedSearch.setUuid("persisted-uuid");
    persistedSearch.setQuery(query);
    persistedSearch.setUser(testUser);

    when(searchRepository.save(any(Search.class))).thenReturn(persistedSearch);

    // When
    Search result = searchService.createSearch(query, testUser);

    // Then
    assertThat(result).isSameAs(persistedSearch);
    assertThat(result.getUuid()).isEqualTo("persisted-uuid");
  }

  @Test
  @DisplayName("Should allow same user to save identical queries")
  void testCreateSearch_DuplicateQueryAllowed() {
    // Given
    String query = "vintage camera";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search search1 = searchService.createSearch(query, testUser);
    Search search2 = searchService.createSearch(query, testUser);

    // Then
    assertThat(search1.getQuery()).isEqualTo(search2.getQuery());
    assertThat(search1.getUser()).isEqualTo(search2.getUser());
    assertThat(search1.getUuid()).isNotEqualTo(search2.getUuid());
    verify(searchRepository, times(2)).save(any(Search.class));
  }

  @Test
  @DisplayName("Should allow different users to save same query")
  void testCreateSearch_SameQueryDifferentUsers() {
    // Given
    String query = "vintage camera";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search search1 = searchService.createSearch(query, testUser);
    Search search2 = searchService.createSearch(query, otherUser);

    // Then
    assertThat(search1.getQuery()).isEqualTo(search2.getQuery());
    assertThat(search1.getUser()).isNotEqualTo(search2.getUser());
    assertThat(search1.getUuid()).isNotEqualTo(search2.getUuid());
  }

  @Test
  @DisplayName("Should preserve query exactly without modification")
  void testCreateSearch_PreservesQueryExactly() {
    // Given
    String queryWithSpaces = "  vintage camera  ";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search result = searchService.createSearch(queryWithSpaces, testUser);

    // Then
    assertThat(result.getQuery()).isEqualTo(queryWithSpaces);
  }

  @Test
  @DisplayName("Should handle query with special characters")
  void testCreateSearch_WithSpecialCharacters() {
    // Given
    String query = "camera $100 & lens <new>";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search result = searchService.createSearch(query, testUser);

    // Then
    assertThat(result.getQuery()).isEqualTo(query);
  }

  @Test
  @DisplayName("Should handle query with Unicode characters")
  void testCreateSearch_WithUnicodeCharacters() {
    // Given
    String query = "vintage 📷 camera 相机 كاميرا";
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search result = searchService.createSearch(query, testUser);

    // Then
    assertThat(result.getQuery()).isEqualTo(query);
  }

  @Test
  @DisplayName("Should handle very long query")
  void testCreateSearch_WithVeryLongQuery() {
    // Given
    String longQuery = "a".repeat(1000);
    when(searchRepository.save(any(Search.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Search result = searchService.createSearch(longQuery, testUser);

    // Then
    assertThat(result.getQuery()).isEqualTo(longQuery);
    assertThat(result.getQuery()).hasSize(1000);
  }

  // =============================
  // GET ALL SEARCHES TESTS
  // =============================

  @Test
  @DisplayName("Should return page of searches for user")
  void testGetAllSearchesForUser_Success() {
    // Given
    Search search1 = createSearch("search-1", "vintage camera", testUser);
    Search search2 = createSearch("search-2", "retro phone", testUser);
    List<Search> searches = Arrays.asList(search1, search2);
    Page<Search> searchPage = new PageImpl<>(searches);
    Pageable pageable = PageRequest.of(0, 10);

    when(searchRepository.findByUser(testUser, pageable)).thenReturn(searchPage);

    // When
    Page<Search> result = searchService.getAllSearchesForUser(testUser, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).containsExactly(search1, search2);
    verify(searchRepository, times(1)).findByUser(testUser, pageable);
  }

  @Test
  @DisplayName("Should return empty page when user has no searches")
  void testGetAllSearchesForUser_EmptyResult() {
    // Given
    Page<Search> emptyPage = Page.empty();
    Pageable pageable = PageRequest.of(0, 10);

    when(searchRepository.findByUser(testUser, pageable)).thenReturn(emptyPage);

    // When
    Page<Search> result = searchService.getAllSearchesForUser(testUser, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
    verify(searchRepository, times(1)).findByUser(testUser, pageable);
  }

  @Test
  @DisplayName("Should call repository with correct parameters")
  void testGetAllSearchesForUser_CallsRepository() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    when(searchRepository.findByUser(testUser, pageable)).thenReturn(Page.empty());

    // When
    searchService.getAllSearchesForUser(testUser, pageable);

    // Then
    verify(searchRepository, times(1)).findByUser(testUser, pageable);
  }

  @Test
  @DisplayName("Should only return searches belonging to the user")
  void testGetAllSearchesForUser_OnlyReturnsUserSearches() {
    // Given
    Search userSearch = createSearch("search-1", "vintage camera", testUser);
    List<Search> searches = Arrays.asList(userSearch);
    Page<Search> searchPage = new PageImpl<>(searches);
    Pageable pageable = PageRequest.of(0, 10);

    when(searchRepository.findByUser(testUser, pageable)).thenReturn(searchPage);

    // When
    Page<Search> result = searchService.getAllSearchesForUser(testUser, pageable);

    // Then
    assertThat(result.getContent()).allMatch(search -> search.getUser().equals(testUser));
    assertThat(result.getContent()).noneMatch(search -> search.getUser().equals(otherUser));
  }

  // =============================
  // GET SEARCH BY UUID TESTS
  // =============================

  @Test
  @DisplayName("Should return search for valid UUID")
  void testGetSearchByUuid_Success() {
    // Given
    String uuid = "search-uuid-123";
    Search search = createSearch(uuid, "vintage camera", testUser);

    when(searchRepository.findById(uuid)).thenReturn(Optional.of(search));

    // When
    Search result = searchService.getSearchByUuid(uuid, testUser);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isEqualTo(uuid);
    assertThat(result.getUser()).isEqualTo(testUser);
    verify(searchRepository, times(1)).findById(uuid);
  }

  @Test
  @DisplayName("Should call repository findById")
  void testGetSearchByUuid_CallsRepositoryFindById() {
    // Given
    String uuid = "search-uuid-123";
    Search search = createSearch(uuid, "vintage camera", testUser);

    when(searchRepository.findById(uuid)).thenReturn(Optional.of(search));

    // When
    searchService.getSearchByUuid(uuid, testUser);

    // Then
    verify(searchRepository, times(1)).findById(uuid);
  }

  @Test
  @DisplayName("Should throw NotFoundException when UUID doesn't exist")
  void testGetSearchByUuid_NotFound_ThrowsNotFoundException() {
    // Given
    String uuid = "non-existent-uuid";
    when(searchRepository.findById(uuid)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> searchService.getSearchByUuid(uuid, testUser))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(uuid);

    verify(searchRepository, times(1)).findById(uuid);
  }

  @Test
  @DisplayName("Should include UUID in NotFoundException message")
  void testGetSearchByUuid_NotFoundExceptionContainsUuid() {
    // Given
    String uuid = "missing-search-123";
    when(searchRepository.findById(uuid)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> searchService.getSearchByUuid(uuid, testUser))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(uuid);
  }

  // =============================
  // DELETE SEARCH TESTS
  // =============================

  @Test
  @DisplayName("Should successfully delete existing search")
  void testDeleteSearch_Success() {
    // Given
    String uuid = "search-uuid-123";
    Search search = createSearch(uuid, "vintage camera", testUser);

    when(searchRepository.findById(uuid)).thenReturn(Optional.of(search));
    doNothing().when(searchRepository).delete(search);

    // When
    searchService.deleteSearch(uuid, testUser);

    // Then
    verify(searchRepository, times(1)).findById(uuid);
    verify(searchRepository, times(1)).delete(search);
  }

  @Test
  @DisplayName("Should call repository delete")
  void testDeleteSearch_CallsRepositoryDelete() {
    // Given
    String uuid = "search-uuid-123";
    Search search = createSearch(uuid, "vintage camera", testUser);

    when(searchRepository.findById(uuid)).thenReturn(Optional.of(search));
    doNothing().when(searchRepository).delete(search);

    // When
    searchService.deleteSearch(uuid, testUser);

    // Then
    ArgumentCaptor<Search> searchCaptor = ArgumentCaptor.forClass(Search.class);
    verify(searchRepository, times(1)).delete(searchCaptor.capture());
    assertThat(searchCaptor.getValue()).isEqualTo(search);
  }

  @Test
  @DisplayName("Should call getSearchByUuid before deleting")
  void testDeleteSearch_CallsGetSearchByUuid() {
    // Given
    String uuid = "search-uuid-123";
    Search search = createSearch(uuid, "vintage camera", testUser);

    when(searchRepository.findById(uuid)).thenReturn(Optional.of(search));
    doNothing().when(searchRepository).delete(search);

    // When
    searchService.deleteSearch(uuid, testUser);

    // Then
    verify(searchRepository, times(1)).findById(uuid);
  }

  @Test
  @DisplayName("Should throw NotFoundException when deleting non-existent search")
  void testDeleteSearch_NotFound_ThrowsNotFoundException() {
    // Given
    String uuid = "non-existent-uuid";
    when(searchRepository.findById(uuid)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> searchService.deleteSearch(uuid, testUser))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(uuid);

    verify(searchRepository, times(1)).findById(uuid);
    verify(searchRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should not call delete when search not found")
  void testDeleteSearch_DoesNotCallDeleteWhenNotFound() {
    // Given
    String uuid = "non-existent-uuid";
    when(searchRepository.findById(uuid)).thenReturn(Optional.empty());

    // When & Then
    try {
      searchService.deleteSearch(uuid, testUser);
    } catch (NotFoundException e) {
      // Expected
    }

    verify(searchRepository, never()).delete(any(Search.class));
  }

}
