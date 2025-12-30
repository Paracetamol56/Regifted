package com.regifted.app.keyword;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordRepository extends JpaRepository<Keyword, String> {
  public Optional<Keyword> findByUuid(String uuid);

  public Optional<Keyword> findByName(String name);

  public Page<Keyword> findByNameContainingIgnoreCase(String name, Pageable pageable);

  public Page<Keyword> findAll(Pageable pageable);

  @Query("""
          SELECT k
          FROM Keyword k
          LEFT JOIN k.items i
          GROUP BY k
          ORDER BY COUNT(i) DESC
      """)
  public List<Keyword> findMostUsedKeywords(int limit);
}
