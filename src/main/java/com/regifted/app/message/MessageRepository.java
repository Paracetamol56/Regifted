package com.regifted.app.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.regifted.app.item.Item;
import com.regifted.app.user.User;

public interface MessageRepository extends JpaRepository<Message, String> {
  Page<Message> findAllBySenderOrReceiverOrderByCreatedAtDesc(User sender, User receiver, Pageable pageable);

  Page<Message> findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(Item item, User sender, User receiver,
      Pageable pageable);

  @Query("""
          SELECT m FROM Message m
          WHERE m.item = :item
            AND m.createdAt IN (
              SELECT MAX(m2.createdAt)
              FROM Message m2
              WHERE m2.item = :item
              GROUP BY
                  CASE
                      WHEN m2.sender.uuid = m2.item.user.uuid THEN m2.receiver.uuid
                      ELSE m2.sender.uuid
                  END
            )
      """)
  Page<Message> findLastMessagesByItemGroupedByParticipant(
      @Param("item") Item item,
      Pageable pageable);

}
