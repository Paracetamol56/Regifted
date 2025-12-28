package com.regifted.app.message;

import static org.junit.jupiter.api.Assertions.*;
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

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.message.dto.ConversationQuery;
import com.regifted.app.message.dto.ConversationResult;
import com.regifted.app.message.dto.ConversationSummaryResponse;
import com.regifted.app.user.User;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock
  private MessageRepository repo;

  @InjectMocks
  private MessageService service;

  private User owner;
  private User requester;
  private User otherUser;
  private Item item;

  @BeforeEach
  void setUp() {
    owner = new User();
    owner.setUuid("owner");
    owner.setName("Owner");

    requester = new User();
    requester.setUuid("requester");
    requester.setName("Requester");

    otherUser = new User();
    otherUser.setUuid("other");
    otherUser.setName("Other");

    item = new Item();
    item.setUuid("item");
    item.setTitle("Item");
    item.setUser(owner);
  }

  // =========================================================
  // getById
  // =========================================================

  @Test
  void getById_shouldReturnMessage_whenExists() {
    Message msg = new Message(
        "id",
        owner,
        otherUser,
        item,
        "Hello",
        Instant.now());

    when(repo.findById("id")).thenReturn(Optional.of(msg));

    Message result = service.getById("id");

    assertEquals(msg, result);
  }

  @Test
  void getById_shouldThrow_whenNotFound() {
    when(repo.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.getById("missing"));
  }

  // =========================================================
  // Conversation summaries
  // =========================================================

  @Test
  void conversationPartner_shouldNeverBeItemOwner() {
    Message msg = new Message();
    msg.setSender(owner);
    msg.setReceiver(otherUser);
    msg.setItem(item);
    msg.setContent("hello");
    msg.setCreatedAt(Instant.now());

    when(repo.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(new PageImpl<>(List.of(msg)));

    Page<ConversationSummaryResponse> page = service.getConversationSummariesForItem(item, PageRequest.of(0, 10));

    ConversationSummaryResponse summary = page.getContent().get(0);

    assertEquals(otherUser.getUuid(), summary.getParticipantUuid());
  }

  // =========================================================
  // Snippet rules
  // =========================================================

  @Test
  void contentLongerThan40_shouldBeTruncated() {
    String content = "a".repeat(41);

    Message msg = new Message();
    msg.setSender(owner);
    msg.setReceiver(otherUser);
    msg.setItem(item);
    msg.setContent(content);
    msg.setCreatedAt(Instant.now());

    when(repo.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(new PageImpl<>(List.of(msg)));

    String snippet = service.getConversationSummariesForItem(item, PageRequest.of(0, 10))
        .getContent().get(0)
        .getLastMessageSnippet();

    assertEquals(41, snippet.length());
    assertTrue(snippet.endsWith("…"));
  }

  @Test
  void contentExactly40_shouldNotBeTruncated() {
    String content = "a".repeat(40);

    Message msg = new Message();
    msg.setSender(owner);
    msg.setReceiver(otherUser);
    msg.setItem(item);
    msg.setContent(content);
    msg.setCreatedAt(Instant.now());

    when(repo.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(new PageImpl<>(List.of(msg)));

    String snippet = service.getConversationSummariesForItem(item, PageRequest.of(0, 10))
        .getContent().get(0)
        .getLastMessageSnippet();

    assertEquals(content, snippet);
  }

  // =========================================================
  // getConversations (business orchestration)
  // =========================================================

  @Test
  void ownerWithoutUser_shouldReceiveConversationSummaries() {
    when(repo.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(Page.empty());

    ConversationResult result = service.getConversations(
        new ConversationQuery(item, owner, null),
        PageRequest.of(0, 10));

    assertTrue(result instanceof ConversationResult.Conversations);
  }

  @Test
  void ownerWithUser_shouldReceiveMessages() {
    when(repo.findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(eq(item), eq(otherUser), eq(otherUser), any()))
        .thenReturn(Page.empty());

    ConversationResult result = service.getConversations(
        new ConversationQuery(item, owner, otherUser),
        PageRequest.of(0, 10));

    assertTrue(result instanceof ConversationResult.Messages);
  }

  @Test
  void nonOwner_shouldOnlySeeOwnConversation_evenIfUserProvided() {
    when(repo.findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(eq(item), eq(requester), eq(requester), any()))
        .thenReturn(Page.empty());

    ConversationResult result = service.getConversations(
        new ConversationQuery(item, requester, otherUser),
        PageRequest.of(0, 10));

    ConversationResult.Messages messages = (ConversationResult.Messages) result;

    assertEquals(requester, messages.participant());
  }

  // =========================================================
  // createMessage – validation rules
  // =========================================================

  @Test
  void createMessage_shouldRejectNullContent() {
    assertThrows(IllegalArgumentException.class,
        () -> service.createMessage(owner, otherUser, item, null));
  }

  @Test
  void createMessage_shouldRejectBlankContent() {
    assertThrows(IllegalArgumentException.class,
        () -> service.createMessage(owner, otherUser, item, "   "));
  }

  @Test
  void createMessage_shouldRejectSenderEqualsReceiver() {
    assertThrows(IllegalArgumentException.class,
        () -> service.createMessage(owner, owner, item, "hello"));
  }

  // =========================================================
  // ⚠ FUTURE RULES (EXPECTED TO FAIL UNTIL IMPLEMENTED)
  // =========================================================

  @Test
  void createMessage_shouldRejectUsersNotRelatedToItem() {
    // requester is neither owner nor participant
    assertThrows(IllegalArgumentException.class,
        () -> service.createMessage(otherUser, requester, item, "hello"));
  }
}
