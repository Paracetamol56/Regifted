package com.regifted.app.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.message.dto.ConversationSummaryResponse;
import com.regifted.app.message.dto.MessageGetResponse;
import com.regifted.app.user.User;

@Service
public class MessageService {

  private final MessageRepository repo;

  public MessageService(MessageRepository repo) {
    this.repo = repo;
  }

  public Message getById(String id) {
    return repo.findById(id)
        .orElseThrow(() -> new NotFoundException(id));
  }

  public Page<ConversationSummaryResponse> getConversationSummariesForItem(
      Item item, Pageable pageable) {

    Page<Message> lastMessages = repo.findLastMessagesByItemGroupedByParticipant(
        item, pageable);

    return lastMessages.map(msg -> {
      User participant = msg.getSender().equals(item.getUser())
          ? msg.getReceiver()
          : msg.getSender();

      return new ConversationSummaryResponse(
          participant.getName(),
          participant.getUuid(),
          item.getTitle(),
          item.getUuid(),
          snippet(msg.getContent()),
          msg.getCreatedAt());
    });
  }

  public Page<MessageGetResponse> getConversationForItemWithUser(Item item, User user, Pageable pageable) {
    Page<Message> page = repo.findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(item, user, user, pageable);

    return page.map(MessageGetResponse::fromMessage);
  }

  public Message createMessage(User sender, User receiver, Item item, String content) {
    validateCreateMessage(sender, receiver, content);

    Message m = new Message();
    m.setContent(content);
    m.setSender(sender);
    m.setReceiver(receiver);
    m.setItem(item);
    return repo.save(m);
  }

  private void validateCreateMessage(User sender, User receiver, String content) {
    if (sender == null || receiver == null) {
      throw new IllegalArgumentException("Sender and receiver must not be null");
    }

    if (sender.equals(receiver)) {
      throw new IllegalArgumentException("Sender cannot send a message to themselves");
    }

    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Message content must not be blank");
    }
  }

  private String snippet(String content) {
    return content.length() > 40
        ? content.substring(0, 40) + "…"
        : content;
  }

}
