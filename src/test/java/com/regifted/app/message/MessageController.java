package com.regifted.app.message;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regifted.app.TestSecurityConfig;
import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.item.ItemService;
import com.regifted.app.message.dto.ConversationResult;
import com.regifted.app.message.dto.MessagePostRequest;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(MessageController.class)
@Import(TestSecurityConfig.class)
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MessageService messageService;

  @MockBean
  private ItemService itemService;

  @MockBean
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  private User owner;
  private User requester;
  private CustomUserPrincipal principal;
  private User participant;
  private Item item;

  @BeforeEach
  void setup() {
    owner = new User();
    owner.setUuid("owner");
    owner.setEmail("owner@regifted.com");
    owner.setName("Owner");
    owner.setPassword("password");

    requester = owner;

    principal = new CustomUserPrincipal(requester);

    participant = new User();
    participant.setUuid("participant");
    participant.setEmail("participant@regifted.com");
    participant.setName("Participant");
    participant.setPassword("password");

    item = new Item();
    item.setUuid("item");
    item.setTitle("Test Item");
    item.setUser(owner);
  }

  // =============================
  // 1. CREATE MESSAGE — HTML
  // =============================

  @Test
  void createMessageHtml_shouldReturnFragment_whenValid() throws Exception {
    MessagePostRequest req = new MessagePostRequest();
    req.setReceiverUuid(participant.getUuid());
    req.setContent("Hello World");

    when(userService.getByUuid(participant.getUuid())).thenReturn(participant);
    when(itemService.getById(item.getUuid())).thenReturn(item);
    when(messageService.createMessage(any(), any(), any(), any()))
        .thenReturn(Mockito.mock(Message.class));

    mockMvc.perform(post("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("receiverUuid", req.getReceiverUuid())
        .param("content", req.getContent()))
        .andExpect(status().isOk())
        .andExpect(view().name("messages/message"))
        .andExpect(model().attributeExists("message"));

    verify(messageService).createMessage(any(), eq(participant), eq(item), eq("Hello World"));
  }

  @Test
  void createMessageHtml_shouldReturn400_whenContentBlank() throws Exception {
    mockMvc.perform(post("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("receiverUuid", participant.getUuid())
        .param("content", " "))
        .andExpect(status().isBadRequest());

    verify(messageService, never()).createMessage(any(), any(), any(), any());
  }

  @Test
  void createMessageHtml_shouldReturn400_whenReceiverMissing() throws Exception {
    mockMvc.perform(post("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("content", "Hello"))
        .andExpect(status().isBadRequest());

    verify(messageService, never()).createMessage(any(), any(), any(), any());
  }

  // =============================
  // 2. CREATE MESSAGE — API
  // =============================

  @Test
  void createMessageApi_shouldReturn201_withLocationAndBody() throws Exception {
    MessagePostRequest req = new MessagePostRequest();
    req.setContent("Hello API");

    when(itemService.getById(item.getUuid())).thenReturn(item);
    Message created = new Message();
    created.setUuid("msg-uuid");
    when(messageService.createMessage(any(), eq(item.getUser()), eq(item), eq(req.getContent())))
        .thenReturn(created);

    mockMvc.perform(post("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/items/" + item.getUuid() + "/messages/" + created.getUuid()))
        .andExpect(jsonPath("$.uuid").value("msg-uuid"));
  }

  @Test
  void createMessageApi_shouldIgnoreReceiverUuid_andSendToItemOwner() throws Exception {
    MessagePostRequest req = new MessagePostRequest();
    req.setContent("Hello API");
    req.setReceiverUuid(participant.getUuid());

    when(itemService.getById(item.getUuid())).thenReturn(item);
    Message created = new Message();
    created.setUuid("msg-uuid");
    when(messageService.createMessage(any(), eq(item.getUser()), eq(item), eq(req.getContent())))
        .thenReturn(created);

    mockMvc.perform(post("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated());

    verify(messageService).createMessage(any(), eq(item.getUser()), eq(item), eq("Hello API"));
  }

  @Test
  void createMessageApi_shouldReturn400_whenContentBlank() throws Exception {
    MessagePostRequest req = new MessagePostRequest();
    req.setContent("  ");

    mockMvc.perform(post("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest());

    verify(messageService, never()).createMessage(any(), any(), any(), any());
  }

  // =============================
  // 3. GET MESSAGES — API
  // =============================

  @Test
  void getMessagesApi_ownerWithoutUser_shouldCallConversationSummaries() throws Exception {
    when(itemService.getById(item.getUuid())).thenReturn(item);
    when(messageService.getConversations(any(), any())).thenReturn(Mockito.mock(ConversationResult.class));

    mockMvc.perform(get("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(messageService).getConversations(any(), any(Pageable.class));
  }

  @Test
  void getMessagesApi_ownerWithUser_shouldCallConversationForUser() throws Exception {
    when(itemService.getById(item.getUuid())).thenReturn(item);
    when(userService.getByUuid("participant")).thenReturn(participant);
    when(messageService.getConversations(any(), any())).thenReturn(Mockito.mock(ConversationResult.class));

    mockMvc.perform(get("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .param("user", "participant")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(userService).getByUuid("participant");
    verify(messageService).getConversations(any(), any(Pageable.class));
  }

  @Test
  void getMessagesApi_nonOwner_shouldOnlySeeOwnConversation() throws Exception {
    when(itemService.getById(item.getUuid())).thenReturn(item);
    when(messageService.getConversations(any(), any())).thenReturn(Mockito.mock(ConversationResult.class));

    mockMvc.perform(get("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(messageService).getConversations(any(), any(Pageable.class));
  }

  // =============================
  // 4. GET MESSAGES — HTML
  // =============================

  @Test
  void getMessagesHtml_ownerWithoutUser_shouldRenderConversationsView() throws Exception {
    when(itemService.getById(item.getUuid())).thenReturn(item);
    ConversationResult result = ConversationResult.conversations(new PageImpl<>(List.of()));
    when(messageService.getConversations(any(), any())).thenReturn(result);

    mockMvc.perform(get("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("messages/conversations"))
        .andExpect(model().attributeExists("conversations"))
        .andExpect(model().attributeExists("item"))
        .andExpect(model().attribute("isOwner", true));
  }

  @Test
  void getMessagesHtml_ownerWithUser_shouldRenderMessagesView() throws Exception {
    when(itemService.getById(item.getUuid())).thenReturn(item);
    ConversationResult result = ConversationResult.messages(new PageImpl<>(List.of()), participant);
    when(messageService.getConversations(any(), any())).thenReturn(result);

    mockMvc.perform(get("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .param("user", "participant")
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("messages/messages"))
        .andExpect(model().attributeExists("messages"))
        .andExpect(model().attributeExists("participant"))
        .andExpect(model().attribute("isOwner", true));
  }

  @Test
  void getMessagesHtml_nonOwner_shouldRenderMessagesView() throws Exception {
    when(itemService.getById(item.getUuid())).thenReturn(item);
    ConversationResult result = ConversationResult.messages(new PageImpl<>(List.of()), requester);
    when(messageService.getConversations(any(), any())).thenReturn(result);

    mockMvc.perform(get("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("messages/messages"))
        .andExpect(model().attributeExists("messages"))
        .andExpect(model().attributeExists("participant"))
        .andExpect(model().attribute("isOwner", false));
  }

  @Test
  void getMessagesApi_shouldReturn404_whenItemNotFound() throws Exception {
    when(itemService.getById(item.getUuid())).thenThrow(new NotFoundException(item.getUuid()));

    mockMvc.perform(get("/items/{itemId}/messages", item.getUuid())
        .with(user(principal))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
