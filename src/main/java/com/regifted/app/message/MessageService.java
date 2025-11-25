package com.regifted.app.message;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.item.ItemRepository;
import com.regifted.app.message.dto.MessagePostRequest;
import com.regifted.app.user.User;
import com.regifted.app.user.UserRepository;

@Service
public class MessageService {

  private final MessageRepository repo;
  private final UserRepository userRepo;
  private final ItemRepository itemRepo;

  public MessageService(
      MessageRepository repo,
      UserRepository userRepo,
      ItemRepository itemRepo) {
    this.repo = repo;
    this.userRepo = userRepo;
    this.itemRepo = itemRepo;
  }

  public Page<Message> getMessages(User user, Pageable pageable) {
    return repo.findAllBySenderOrReceiverOrderByCreatedAtDesc(user, user, pageable);
  }

  public Page<Message> getLastMessagesForUser(User user, Pageable pageable) {
    return repo.findDistinctByItemOrderByCreatedAtDesc(user, pageable);
  }

  public Page<Message> getLastMessagesForItem(User user, String itemId, Pageable pageable) {
    Item item = itemRepo.findById(itemId)
        .orElseThrow(() -> new NotFoundException(itemId));

    if (!item.getUser().getUuid().equals(user.getUuid())) {
      throw new NotFoundException(itemId);
    }

    return repo.findDistinctByItemOrderByCreatedAtDesc(item, pageable);
  }

  public Message getById(String id) {
    return repo.findById(id)
        .orElseThrow(() -> new NotFoundException(id));
  }

  public Message createMessage(User sender, MessagePostRequest req) {
    Message m = new Message();
    m.setUuid(UUID.randomUUID().toString());
    m.setCreatedAt(Instant.now());
    m.setContent(req.getContent());
    m.setSender(sender);
    m.setReceiver(userRepo.getReferenceById(req.getReceiver()));
    m.setItem(itemRepo.getReferenceById(req.getItem()));
    return repo.save(m);
  }
}
