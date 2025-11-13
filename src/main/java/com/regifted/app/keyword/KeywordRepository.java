package com.regifted.app.keyword;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, String> {
    Optional<Keyword> findByName(String name);
}