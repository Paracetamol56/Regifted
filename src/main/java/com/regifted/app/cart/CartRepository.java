package com.regifted.app.cart;

import com.regifted.app.user.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
  Page<Cart> findAllByUser(User user, Pageable pageable);
}
