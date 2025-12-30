
package com.regifted.app.keyword;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;

@Service
public class KeywordService {

  private final KeywordRepository keywordRepository;

  public KeywordService(KeywordRepository keywordRepository) {
    this.keywordRepository = keywordRepository;
  }

  public Page<Keyword> getAllKeywords(Pageable pageable) {
    return keywordRepository.findAll(pageable);
  }

  public Page<Keyword> searchKeywords(String query, Pageable pageable) {
    String searchQuery = query.trim().toLowerCase();
    if (searchQuery.isEmpty()) {
      return keywordRepository.findAll(pageable);
    }
    return keywordRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
  }

  public Keyword getByUuid(String uuid) {
    if (uuid == null || uuid.isEmpty()) {
      throw new NotFoundException("null");
    }

    return keywordRepository.findByUuid(uuid)
        .orElseThrow(() -> new NotFoundException(uuid.toString()));
  }

  public List<Keyword> getMostUsedKeywords(int limit) {
    return keywordRepository.findMostUsedKeywords(limit);
  }

  public Keyword getByName(String name) {
    return keywordRepository.findByName(name).orElse(null);
  }

  public Keyword getOrCreateKeyword(String name) {
    name = name.trim().toLowerCase();
    if (name == null || name.isEmpty()) {
      return null;
    }

    Optional<Keyword> existing = keywordRepository.findByName(name);
    if (existing.isPresent()) {
      return existing.get();
    }

    Keyword keyword = new Keyword();
    keyword.setName(name);
    return keywordRepository.save(keyword);
  }
}
