package com.regifted.app.message;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.item.Item;
import com.regifted.app.item.ItemService;
import com.regifted.app.message.dto.ConversationQuery;
import com.regifted.app.message.dto.ConversationResult;
import com.regifted.app.message.dto.MessageGetResponse;
import com.regifted.app.message.dto.MessagePostRequest;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

import jakarta.validation.Valid;

@RestController
public class MessageController {

  private final MessageService messageService;
  private final ItemService itemService;
  private final UserService userService;

  public MessageController(MessageService messageService, ItemService itemService, UserService userService) {
    this.messageService = messageService;
    this.itemService = itemService;
    this.userService = userService;
  }

  // =============================
  // CREATE MESSAGE
  // =============================

  @PostMapping(value = "/items/{itemId}/messages", consumes = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE,
      MediaType.APPLICATION_FORM_URLENCODED_VALUE }, produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView createMessageHtml(
      @PathVariable String itemId,
      @Valid MessagePostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal,
      Model model) {

    Message created = messageService.createMessage(
        principal.getUser(),
        userService.getByUuid(req.getReceiverUuid()),
        itemService.getById(itemId),
        req.getContent());

    model.addAttribute("message", MessageGetResponse.fromMessage(created));

    return new ModelAndView("messages/message", model.asMap());
  }

  @PostMapping(value = "/items/{itemId}/messages", consumes = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
          MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<MessageGetResponse> createMessageApi(
      @PathVariable String itemId,
      @Valid @RequestBody MessagePostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Message created = messageService.createMessage(
        principal.getUser(),
        itemService.getById(itemId).getUser(),
        itemService.getById(itemId),
        req.getContent());

    URI location = URI.create("/items/" + itemId + "/messages/" + created.getUuid());

    return ResponseEntity.created(location)
        .body(MessageGetResponse.fromMessage(created));
  }

  // =============================
  // GET MESSAGES — API
  // =============================

  @GetMapping(value = "/items/{itemId}/messages", produces = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE })
  public ConversationResult getMessagesForItemApi(
      @PathVariable String itemId,
      @RequestParam(name = "user", required = false) String userId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Item item = itemService.getById(itemId);
    User requester = principal.getUser();
    User requested = userId != null ? userService.getByUuid(userId) : null;

    return messageService.getConversations(
        new ConversationQuery(item, requester, requested),
        pageable);
  }

  // =============================
  // GET MESSAGES — HTML
  // =============================

  @GetMapping(value = "/items/{itemId}/messages", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView getMessagesForItemHtml(
      @PathVariable String itemId,
      @RequestParam(name = "user", required = false) String userId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      Model model,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Item item = itemService.getById(itemId);
    User requester = principal.getUser();
    User requested = userId != null ? userService.getByUuid(userId) : null;

    ConversationResult result = messageService.getConversations(
        new ConversationQuery(item, requester, requested),
        pageable);

    model.addAttribute("item", item);
    model.addAttribute("isOwner", requester.equals(item.getUser()));

    if (result instanceof ConversationResult.Conversations c) {
      model.addAttribute("conversations", c.page());
      return new ModelAndView("messages/conversations", model.asMap());
    }

    ConversationResult.Messages m = (ConversationResult.Messages) result;
    model.addAttribute("messages", m.page());
    model.addAttribute("participant", m.participant());
    return new ModelAndView("messages/messages", model.asMap());
  }
}
