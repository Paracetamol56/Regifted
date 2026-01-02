package com.regifted.app.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.regifted.app.item.Item;
import com.regifted.app.item.ItemService;
import com.regifted.app.message.dto.ConversationQuery;
import com.regifted.app.message.dto.ConversationResult;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private UserService userService;

    private User sender;
    private User receiver;
    private Item mockItem;
    private CustomUserPrincipal senderPrincipal;
    private Message mockMessage;

    @BeforeEach
    void setUp() {
        // Mock des utilisateurs
        sender = new User();
        sender.setUuid(UUID.randomUUID().toString());
        sender.setEmail("sender@test.com");
        senderPrincipal = new CustomUserPrincipal(sender);

        receiver = new User();
        receiver.setUuid(UUID.randomUUID().toString());

        // Mock de l'item
        mockItem = new Item();
        mockItem.setUuid("item-123");
        mockItem.setUser(receiver); // Le receiver est le propriétaire de l'objet

        // Mock d'un message
        mockMessage = new Message();
        mockMessage.setUuid("msg-456");
        mockMessage.setContent("Hello, is it available?");
        mockMessage.setSender(sender);
        mockMessage.setReceiver(receiver);
        mockMessage.setItem(mockItem);
        mockMessage.setCreatedAt(Instant.now());
    }

    // =============================
    // TESTS CREATE MESSAGE
    // =============================

    @Test
    @DisplayName("HTML POST: Créer un message et renvoyer la vue fragment")
    void createMessageHtml_Success() throws Exception {
        when(userService.getByUuid(anyString())).thenReturn(receiver);
        when(itemService.getById(anyString())).thenReturn(mockItem);
        when(messageService.createMessage(any(), any(), any(), anyString())).thenReturn(mockMessage);

        mockMvc.perform(post("/items/item-123/messages")
                .with(user(senderPrincipal))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("receiverUuid", receiver.getUuid())
                .param("content", "Hello!"))
                .andExpect(status().isOk())
                .andExpect(view().name("messages/message"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    @DisplayName("API POST: Créer un message JSON")
    void createMessageApi_Success() throws Exception {
        when(itemService.getById("item-123")).thenReturn(mockItem);
        when(messageService.createMessage(any(), any(), any(), anyString())).thenReturn(mockMessage);

        String jsonBody = "{\"content\": \"Hello API\", \"receiverUuid\": \"" + receiver.getUuid() + "\"}";

        mockMvc.perform(post("/items/item-123/messages")
                .with(user(senderPrincipal))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value("msg-456"))
                .andExpect(jsonPath("$.content").value("Hello, is it available?"));
    }

    // =============================
    // TESTS GET CONVERSATIONS / MESSAGES
    // =============================

    @Test
    @DisplayName("HTML GET: Affichage des messages (Vue messages)")
    void getMessagesForItemHtml_MessagesView() throws Exception {
        when(itemService.getById("item-123")).thenReturn(mockItem);
        
        // Simuler un résultat de type Messages (conversation spécifique)
        ConversationResult.Messages mockResult = new ConversationResult.Messages(
                org.springframework.data.domain.Page.empty(),
                receiver
        );
        
        when(messageService.getConversations(any(ConversationQuery.class), any(Pageable.class)))
            .thenReturn(mockResult);

        mockMvc.perform(get("/items/item-123/messages")
                .with(user(senderPrincipal))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("messages/messages"))
                .andExpect(model().attributeExists("messages"))
                .andExpect(model().attribute("participant", receiver));
    }

    @Test
    @DisplayName("API GET: Récupérer les conversations JSON")
    void getMessagesForItemApi_Success() throws Exception {
        when(itemService.getById("item-123")).thenReturn(mockItem);
        
        // On peut retourner null ou un mock vide pour l'API
        when(messageService.getConversations(any(ConversationQuery.class), any(Pageable.class)))
            .thenReturn(null); 

        mockMvc.perform(get("/items/item-123/messages")
                .with(user(senderPrincipal))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}