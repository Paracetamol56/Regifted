package com.regifted.app.keyword;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class KeywordService {

  private final KeywordRepository keywordRepository;

  public KeywordService(KeywordRepository keywordRepository) {
    this.keywordRepository = keywordRepository;
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
