package com.regifted.app.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.regifted.app.keyword.Keyword;

public interface ItemRepository extends JpaRepository<Item, String> {
  @Query("""
          SELECT i FROM Item i
          WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))
      """)
  Page<Item> searchByTitleOrDescription(@Param("query") String query, Pageable pageable);

  Page<Item> findByKeywordsContaining(Keyword keyword, Pageable pageable);
}
