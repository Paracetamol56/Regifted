package com.regifted.app.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.regifted.app.item.Item;
import com.regifted.app.user.User;

public interface MessageRepository extends JpaRepository<Message, String> {
  Page<Message> findAllBySenderOrReceiverOrderByCreatedAtDesc(User sender, User receiver, Pageable pageable);

  // Find the last message for each distinct item owned by the provided user
  Page<Message> findDistinctByItemOrderByCreatedAtDesc(User user, Pageable pageable);

  // Find the last messaged for each distinct user associated with the provided
  // item
  Page<Message> findDistinctByItemOrderByCreatedAtDesc(Item item, Pageable pageable);
}
