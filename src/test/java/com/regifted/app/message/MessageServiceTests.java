package com.regifted.app.message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.message.dto.ConversationSummaryResponse;
import com.regifted.app.message.dto.MessageGetResponse;
import com.regifted.app.user.User;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @InjectMocks
  private MessageService messageService;

  private User sender;
  private User receiver;
  private Item item;

  @BeforeEach
  void setUp() {
    sender = new User();
    sender.setUuid("sender-id");
    sender.setName("Sender");

    receiver = new User();
    receiver.setUuid("receiver-id");
    receiver.setName("Receiver");

    item = new Item();
    item.setUuid("item-id");
    item.setTitle("Item title");
    item.setUser(sender);
  }

  /*
   * ------------------------------------------------------------------
   * getById
   * ------------------------------------------------------------------
   */

  @Test
  void getById_shouldReturnMessage_whenExists() {
    Message message = new Message(
        "msg-id",
        sender,
        receiver,
        item,
        "Hello",
        Instant.now());

    when(messageRepository.findById("msg-id"))
        .thenReturn(Optional.of(message));

    Message result = messageService.getById("msg-id");

    assertEquals(message, result);
    verify(messageRepository).findById("msg-id");
  }

  @Test
  void getById_shouldThrowNotFoundException_whenNotExists() {
    when(messageRepository.findById("missing-id"))
        .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> messageService.getById("missing-id"));
  }

  /*
   * ------------------------------------------------------------------
   * getConversationSummariesForItem
   * ------------------------------------------------------------------
   */

  @Test
  void getConversationSummaries_shouldReturnEmptyPage_whenNoMessages() {
    Pageable pageable = PageRequest.of(0, 10);

    when(messageRepository.findLastMessagesByItemGroupedByParticipant(item, pageable))
        .thenReturn(Page.empty());

    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    assertTrue(result.isEmpty());
  }

  @Test
  void getConversationSummaries_shouldUseReceiverAsParticipant_whenSenderIsItemOwner() {
    Message message = new Message();
    message.setSender(sender);
    message.setReceiver(receiver);
    message.setItem(item);
    message.setContent("Hello");
    message.setCreatedAt(Instant.now());

    Pageable pageable = PageRequest.of(0, 10);

    when(messageRepository.findLastMessagesByItemGroupedByParticipant(item, pageable))
        .thenReturn(new PageImpl<>(List.of(message)));

    ConversationSummaryResponse summary = messageService.getConversationSummariesForItem(item, pageable)
        .getContent().get(0);

    assertEquals("Receiver", summary.getParticipantName());
    assertEquals("receiver-id", summary.getParticipantUuid());
  }

  @Test
  void getConversationSummaries_shouldTruncateContentLongerThan40Chars() {
    String longContent = "a".repeat(41);

    Message message = new Message();
    message.setSender(sender);
    message.setReceiver(receiver);
    message.setItem(item);
    message.setContent(longContent);
    message.setCreatedAt(Instant.now());

    Pageable pageable = PageRequest.of(0, 10);

    when(messageRepository.findLastMessagesByItemGroupedByParticipant(item, pageable))
        .thenReturn(new PageImpl<>(List.of(message)));

    ConversationSummaryResponse summary = messageService.getConversationSummariesForItem(item, pageable)
        .getContent().get(0);

    assertEquals(41, summary.getLastMessageSnippet().length());
    assertTrue(summary.getLastMessageSnippet().endsWith("…"));
  }

  @Test
  void getConversationSummaries_shouldNotTruncateContentExactly40Chars() {
    String exactContent = "a".repeat(40);

    Message message = new Message();
    message.setSender(sender);
    message.setReceiver(receiver);
    message.setItem(item);
    message.setContent(exactContent);
    message.setCreatedAt(Instant.now());

    Pageable pageable = PageRequest.of(0, 10);

    when(messageRepository.findLastMessagesByItemGroupedByParticipant(item, pageable))
        .thenReturn(new PageImpl<>(List.of(message)));

    ConversationSummaryResponse summary = messageService.getConversationSummariesForItem(item, pageable)
        .getContent().get(0);

    assertEquals(exactContent, summary.getLastMessageSnippet());
  }

  /*
   * ------------------------------------------------------------------
   * getConversationForItemWithUser
   * ------------------------------------------------------------------
   */

  @Test
  void getConversationForItemWithUser_shouldMapMessagesToResponses() {
    Message message = new Message();
    message.setSender(sender);
    message.setReceiver(receiver);
    message.setItem(item);

    Pageable pageable = PageRequest.of(0, 10);

    when(messageRepository
        .findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(item, receiver, receiver, pageable))
        .thenReturn(new PageImpl<>(List.of(message)));

    Page<MessageGetResponse> result = messageService.getConversationForItemWithUser(item, receiver, pageable);

    assertEquals(1, result.getTotalElements());
  }

  /*
   * ------------------------------------------------------------------
   * createMessage
   * ------------------------------------------------------------------
   */

  @Test
  void createMessage_shouldSaveMessage_whenValid() {
    when(messageRepository.save(any(Message.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Message result = messageService.createMessage(sender, receiver, item, "Hello");

    assertEquals("Hello", result.getContent());
    assertEquals(sender, result.getSender());
    assertEquals(receiver, result.getReceiver());
    assertEquals(item, result.getItem());
  }

  @Test
  void createMessage_shouldThrowException_whenSenderAndReceiverAreSame() {
    assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(sender, sender, item, "Hello"));

    verify(messageRepository, never()).save(any());
  }

  @Test
  void createMessage_shouldThrowException_whenContentIsNull() {
    assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(sender, receiver, item, null));

    verify(messageRepository, never()).save(any());
  }

  @Test
  void createMessage_shouldThrowException_whenContentIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(sender, receiver, item, ""));

    verify(messageRepository, never()).save(any());
  }

  @Test
  void createMessage_shouldThrowException_whenContentIsBlank() {
    assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(sender, receiver, item, "   "));

    verify(messageRepository, never()).save(any());
  }
}
