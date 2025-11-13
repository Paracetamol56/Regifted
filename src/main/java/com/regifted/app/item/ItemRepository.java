package com.regifted.app.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, String> {
  @Query(value = """
        SELECT * FROM item
        WHERE LOWER(title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%'))
      """, countQuery = """
        SELECT COUNT(*) FROM item
        WHERE LOWER(title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%'))
      """, nativeQuery = true)
  Page<Item> searchByTitleOrDescription(@Param("query") String query, Pageable pageable);
}
