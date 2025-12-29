package com.regifted.app.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.regifted.app.user.User;

@Repository
public interface SearchRepository extends JpaRepository<Search, String> {
  Page<Search> findByUser(User user, Pageable pageable);
}
