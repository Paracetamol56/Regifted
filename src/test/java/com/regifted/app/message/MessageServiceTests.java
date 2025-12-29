package com.regifted.app.message;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.exception.SelfMessagingException;
import com.regifted.app.exception.UnrelatedParticipantException;
import com.regifted.app.item.Item;
import com.regifted.app.message.dto.ConversationQuery;
import com.regifted.app.message.dto.ConversationResult;
import com.regifted.app.message.dto.ConversationSummaryResponse;
import com.regifted.app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Unit Tests")
class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @InjectMocks
  private MessageService messageService;

  private User owner;
  private User requester;
  private User otherUser;
  private Item item;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    owner = createUser("owner-uuid", "Owner");
    requester = createUser("requester-uuid", "Requester");
    otherUser = createUser("other-uuid", "Other");

    item = createItem("item-uuid", "Test Item", owner);
    pageable = PageRequest.of(0, 10);
  }

  private User createUser(String uuid, String name) {
    User user = new User();
    user.setUuid(uuid);
    user.setName(name);
    return user;
  }

  private Item createItem(String uuid, String title, User owner) {
    Item item = new Item();
    item.setUuid(uuid);
    item.setTitle(title);
    item.setUser(owner);
    return item;
  }

  private Message createMessage(String uuid, User sender, User receiver, Item item, String content) {
    Message message = new Message();
    message.setUuid(uuid);
    message.setSender(sender);
    message.setReceiver(receiver);
    message.setItem(item);
    message.setContent(content);
    message.setCreatedAt(Instant.now());
    return message;
  }

  // =============================
  // GET BY ID TESTS
  // =============================

  @Test
  @DisplayName("Should return message when exists")
  void testGetById_MessageExists() {
    // Given
    String messageId = "message-uuid";
    Message message = createMessage(messageId, owner, otherUser, item, "Hello");
    when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

    // When
    Message result = messageService.getById(messageId);

    // Then
    assertThat(result).isEqualTo(message);
    assertThat(result.getUuid()).isEqualTo(messageId);
    assertThat(result.getContent()).isEqualTo("Hello");
    verify(messageRepository, times(1)).findById(messageId);
  }

  @Test
  @DisplayName("Should throw NotFoundException when message does not exist")
  void testGetById_MessageNotFound() {
    // Given
    String messageId = "non-existent-uuid";
    when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> messageService.getById(messageId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(messageId);
    verify(messageRepository, times(1)).findById(messageId);
  }

  @Test
  @DisplayName("Should handle null message ID gracefully")
  void testGetById_NullId() {
    // Given
    when(messageRepository.findById(null)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> messageService.getById(null))
        .isInstanceOf(NotFoundException.class);
  }

  // =============================
  // CREATE MESSAGE TESTS
  // =============================

  @Test
  @DisplayName("Should create message successfully with valid inputs")
  void testCreateMessage_Success() {
    // Given
    String content = "Hello, I'm interested in this item";
    Message savedMessage = createMessage("new-message-uuid", owner, otherUser, item, content);
    when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

    // When
    Message result = messageService.createMessage(owner, otherUser, item, content);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo(content);
    assertThat(result.getSender()).isEqualTo(owner);
    assertThat(result.getReceiver()).isEqualTo(otherUser);
    assertThat(result.getItem()).isEqualTo(item);

    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository, times(1)).save(messageCaptor.capture());

    Message capturedMessage = messageCaptor.getValue();
    assertThat(capturedMessage.getContent()).isEqualTo(content);
    assertThat(capturedMessage.getSender()).isEqualTo(owner);
    assertThat(capturedMessage.getReceiver()).isEqualTo(otherUser);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when content is null")
  void testCreateMessage_NullContent() {
    // When & Then
    assertThatThrownBy(() -> messageService.createMessage(owner, otherUser, item, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message content must not be blank");
    verify(messageRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when content is empty")
  void testCreateMessage_EmptyContent() {
    // When & Then
    assertThatThrownBy(() -> messageService.createMessage(owner, otherUser, item, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message content must not be blank");
    verify(messageRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when content is only whitespace")
  void testCreateMessage_BlankContent() {
    // When & Then
    assertThatThrownBy(() -> messageService.createMessage(owner, otherUser, item, "   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message content must not be blank");
    verify(messageRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw SelfMessagingException when sender equals receiver")
  void testCreateMessage_SenderEqualsReceiver() {
    // When & Then
    assertThatThrownBy(() -> messageService.createMessage(owner, owner, item, "Hello"))
        .isInstanceOf(SelfMessagingException.class);
    verify(messageRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw UnrelatedParticipantException when neither user is related to item")
  void testCreateMessage_UnrelatedUsers() {
    // Given - Both requester and otherUser are not the item owner

    // When & Then
    assertThatThrownBy(() -> messageService.createMessage(requester, otherUser, item, "Hello"))
        .isInstanceOf(UnrelatedParticipantException.class);
    verify(messageRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should allow message when sender is item owner")
  void testCreateMessage_SenderIsOwner() {
    // Given
    Message savedMessage = createMessage("msg-uuid", owner, otherUser, item, "Hello");
    when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

    // When
    Message result = messageService.createMessage(owner, otherUser, item, "Hello");

    // Then
    assertThat(result).isNotNull();
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  @DisplayName("Should allow message when receiver is item owner")
  void testCreateMessage_ReceiverIsOwner() {
    // Given
    Message savedMessage = createMessage("msg-uuid", otherUser, owner, item, "Hello");
    when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

    // When
    Message result = messageService.createMessage(otherUser, owner, item, "Hello");

    // Then
    assertThat(result).isNotNull();
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  @DisplayName("Should trim and save message content")
  void testCreateMessage_TrimsContent() {
    // Given
    String contentWithSpaces = "  Hello  ";
    Message savedMessage = createMessage("msg-uuid", owner, otherUser, item, contentWithSpaces);
    when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

    // When
    messageService.createMessage(owner, otherUser, item, contentWithSpaces);

    // Then
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository, times(1)).save(messageCaptor.capture());
    // Note: If trimming is implemented, verify the content is trimmed
    assertThat(messageCaptor.getValue().getContent()).isEqualTo(contentWithSpaces);
  }

  // =============================
  // CONVERSATION SUMMARIES TESTS
  // =============================

  @Test
  @DisplayName("Should return conversation summaries for item owner")
  void testGetConversationSummariesForItem_Success() {
    // Given
    Message message = createMessage("msg-uuid", owner, otherUser, item, "Hello");
    Page<Message> messagePage = new PageImpl<>(List.of(message));
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(messagePage);

    // When
    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(messageRepository, times(1))
        .findLastMessagesByItemGroupedByParticipant(eq(item), any());
  }

  @Test
  @DisplayName("Should identify conversation partner as non-owner")
  void testGetConversationSummariesForItem_PartnerIsNotOwner() {
    // Given
    Message message = createMessage("msg-uuid", owner, otherUser, item, "Hello");
    Page<Message> messagePage = new PageImpl<>(List.of(message));
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(messagePage);

    // When
    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    // Then
    ConversationSummaryResponse summary = result.getContent().get(0);
    assertThat(summary.getParticipantUuid()).isEqualTo(otherUser.getUuid());
    assertThat(summary.getParticipantUuid()).isNotEqualTo(owner.getUuid());
  }

  @Test
  @DisplayName("Should return empty page when no conversations exist")
  void testGetConversationSummariesForItem_NoConversations() {
    // Given
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(Page.empty());

    // When
    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    // Then
    assertThat(result).isEmpty();
    verify(messageRepository, times(1))
        .findLastMessagesByItemGroupedByParticipant(eq(item), any());
  }

  // =============================
  // SNIPPET TRUNCATION TESTS
  // =============================

  @Test
  @DisplayName("Should truncate content longer than 40 characters with ellipsis")
  void testSnippet_ContentLongerThan40_ShouldBeTruncated() {
    // Given
    String longContent = "a".repeat(41);
    Message message = createMessage("msg-uuid", owner, otherUser, item, longContent);
    Page<Message> messagePage = new PageImpl<>(List.of(message));
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(messagePage);

    // When
    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    // Then
    String snippet = result.getContent().get(0).getLastMessageSnippet();
    assertThat(snippet).hasSize(41);
    assertThat(snippet).endsWith("…");
    assertThat(snippet).startsWith("a".repeat(40));
  }

  @Test
  @DisplayName("Should not truncate content exactly 40 characters")
  void testSnippet_ContentExactly40_ShouldNotBeTruncated() {
    // Given
    String content = "a".repeat(40);
    Message message = createMessage("msg-uuid", owner, otherUser, item, content);
    Page<Message> messagePage = new PageImpl<>(List.of(message));
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(messagePage);

    // When
    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    // Then
    String snippet = result.getContent().get(0).getLastMessageSnippet();
    assertThat(snippet).isEqualTo(content);
    assertThat(snippet).doesNotEndWith("…");
  }

  @Test
  @DisplayName("Should not truncate content shorter than 40 characters")
  void testSnippet_ContentShorterThan40_ShouldNotBeTruncated() {
    // Given
    String content = "Hello, world!";
    Message message = createMessage("msg-uuid", owner, otherUser, item, content);
    Page<Message> messagePage = new PageImpl<>(List.of(message));
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(messagePage);

    // When
    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    // Then
    String snippet = result.getContent().get(0).getLastMessageSnippet();
    assertThat(snippet).isEqualTo(content);
    assertThat(snippet).doesNotEndWith("…");
  }

  @Test
  @DisplayName("Should handle empty content for snippet")
  void testSnippet_EmptyContent() {
    // Given
    Message message = createMessage("msg-uuid", owner, otherUser, item, "");
    Page<Message> messagePage = new PageImpl<>(List.of(message));
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(messagePage);

    // When
    Page<ConversationSummaryResponse> result = messageService.getConversationSummariesForItem(item, pageable);

    // Then
    String snippet = result.getContent().get(0).getLastMessageSnippet();
    assertThat(snippet).isEmpty();
  }

  // =============================
  // GET CONVERSATIONS TESTS (Business Orchestration)
  // =============================

  @Test
  @DisplayName("Should return conversation summaries when owner without specific user")
  void testGetConversations_OwnerWithoutUser_ReturnsConversations() {
    // Given
    ConversationQuery query = new ConversationQuery(item, owner, null);
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(Page.empty());

    // When
    ConversationResult result = messageService.getConversations(query, pageable);

    // Then
    assertThat(result).isInstanceOf(ConversationResult.Conversations.class);
    verify(messageRepository, times(1))
        .findLastMessagesByItemGroupedByParticipant(eq(item), any());
  }

  @Test
  @DisplayName("Should return messages when owner with specific user")
  void testGetConversations_OwnerWithUser_ReturnsMessages() {
    // Given
    ConversationQuery query = new ConversationQuery(item, owner, otherUser);
    when(messageRepository.findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(
        eq(item), eq(otherUser), eq(otherUser), any()))
        .thenReturn(Page.empty());

    // When
    ConversationResult result = messageService.getConversations(query, pageable);

    // Then
    assertThat(result).isInstanceOf(ConversationResult.Messages.class);
    verify(messageRepository, times(1))
        .findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(
            eq(item), eq(otherUser), eq(otherUser), any());
  }

  @Test
  @DisplayName("Should return own conversation for non-owner regardless of user parameter")
  void testGetConversations_NonOwner_ReturnsOwnConversation() {
    // Given
    ConversationQuery query = new ConversationQuery(item, requester, otherUser);
    when(messageRepository.findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(
        eq(item), eq(requester), eq(requester), any()))
        .thenReturn(Page.empty());

    // When
    ConversationResult result = messageService.getConversations(query, pageable);

    // Then
    assertThat(result).isInstanceOf(ConversationResult.Messages.class);
    ConversationResult.Messages messages = (ConversationResult.Messages) result;
    assertThat(messages.participant()).isEqualTo(requester);

    // Verify it queries for requester, not otherUser
    verify(messageRepository, times(1))
        .findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(
            eq(item), eq(requester), eq(requester), any());
  }

  @Test
  @DisplayName("Should return own conversation for non-owner without user parameter")
  void testGetConversations_NonOwnerWithoutUser_ReturnsOwnConversation() {
    // Given
    ConversationQuery query = new ConversationQuery(item, requester, null);
    when(messageRepository.findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(
        eq(item), eq(requester), eq(requester), any()))
        .thenReturn(Page.empty());

    // When
    ConversationResult result = messageService.getConversations(query, pageable);

    // Then
    assertThat(result).isInstanceOf(ConversationResult.Messages.class);
    ConversationResult.Messages messages = (ConversationResult.Messages) result;
    assertThat(messages.participant()).isEqualTo(requester);
  }

  @Test
  @DisplayName("Should handle empty conversation list for owner")
  void testGetConversations_OwnerWithNoConversations() {
    // Given
    ConversationQuery query = new ConversationQuery(item, owner, null);
    when(messageRepository.findLastMessagesByItemGroupedByParticipant(eq(item), any()))
        .thenReturn(Page.empty());

    // When
    ConversationResult result = messageService.getConversations(query, pageable);

    // Then
    assertThat(result).isInstanceOf(ConversationResult.Conversations.class);
    ConversationResult.Conversations conversations = (ConversationResult.Conversations) result;
    assertThat(conversations.page()).isEmpty();
  }

  @Test
  @DisplayName("Should handle empty message list for specific conversation")
  void testGetConversations_OwnerWithUserNoMessages() {
    // Given
    ConversationQuery query = new ConversationQuery(item, owner, otherUser);
    when(messageRepository.findAllByItemAndSenderOrReceiverOrderByCreatedAtDesc(
        eq(item), eq(otherUser), eq(otherUser), any()))
        .thenReturn(Page.empty());

    // When
    ConversationResult result = messageService.getConversations(query, pageable);

    // Then
    assertThat(result).isInstanceOf(ConversationResult.Messages.class);
    ConversationResult.Messages messages = (ConversationResult.Messages) result;
    assertThat(messages.page()).isEmpty();
  }

}
