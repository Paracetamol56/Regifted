package com.regifted.app.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.exception.SelfMessagingException;
import com.regifted.app.exception.UnrelatedParticipantException;
import com.regifted.app.item.Item;
import com.regifted.app.message.dto.ConversationQuery;
import com.regifted.app.message.dto.ConversationResult;
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

  public ConversationResult getConversations(
      ConversationQuery query,
      Pageable pageable) {

    Item item = query.item();
    User requester = query.requester();
    User owner = item.getUser();
    User requested = query.requestedParticipant();

    boolean isOwner = requester.equals(owner);

    // CASE 1 — OWNER: ALL CONVERSATIONS
    if (isOwner && requested == null) {
      Page<ConversationSummaryResponse> conversations = getConversationSummariesForItem(item, pageable);

      return ConversationResult.conversations(conversations);
    }

    // CASE 2 — OWNER WITH SPECIFIC USER
    if (isOwner && requested != null) {
      Page<MessageGetResponse> messages = getConversationForItemWithUser(item, requested, pageable);

      return ConversationResult.messages(messages, requested);
    }

    // CASE 3 — NON-OWNER → ONLY OWN CONVERSATION
    Page<MessageGetResponse> messages = getConversationForItemWithUser(item, requester, pageable);

    return ConversationResult.messages(messages, requester);
  }

  public Page<ConversationSummaryResponse> getConversationSummariesForItem(
      Item item, Pageable pageable) {
    return repo.findLastMessagesByItemGroupedByParticipant(item, pageable)
        .map(msg -> {
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

  public Page<MessageGetResponse> getConversationForItemWithUser(
      Item item, User user, Pageable pageable) {
    return repo
        .findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(item, user, user, pageable)
        .map(MessageGetResponse::from);
  }

  public Message createMessage(User sender, User receiver, Item item, String content) {
    validateCreateMessage(sender, receiver, item, content);

    Message m = new Message();
    m.setContent(content);
    m.setSender(sender);
    m.setReceiver(receiver);
    m.setItem(item);
    return repo.save(m);
  }

  private void validateCreateMessage(User sender, User receiver, Item item, String content) {

    if (sender.equals(receiver)) {
      throw new SelfMessagingException(sender.getUuid());
    }

    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Message content must not be blank");
    }

    User owner = item.getUser();
    boolean senderRelated = sender.equals(owner);
    boolean receiverRelated = receiver.equals(owner);

    if (!senderRelated && !receiverRelated) {
      throw new UnrelatedParticipantException(
          item.getUuid(),
          sender.getUuid(),
          receiver.getUuid());

    }
  }

  private String snippet(String content) {
    return content.length() > 40
        ? content.substring(0, 40) + "…"
        : content;
  }

}
