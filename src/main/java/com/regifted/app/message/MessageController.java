package com.regifted.app.message;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.Item;
import com.regifted.app.item.ItemService;
import com.regifted.app.message.dto.ConversationSummaryResponse;
import com.regifted.app.message.dto.MessageGetResponse;
import com.regifted.app.message.dto.MessagePostRequest;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
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

  private Message createMessage(String itemId, MessagePostRequest req, User sender) {
    Item item = itemService.getById(itemId);
    if (item == null) {
      throw new NotFoundException(itemId);
    }
    User receiver = userService.getByUuid(req.getReceiver().toString());
    // If the sender is not the owner, the receiver must be the owner
    if (!sender.getUuid().equals(item.getUser().getUuid())
        && !receiver.getUuid().equals(item.getUser().getUuid())) {
      throw new IllegalArgumentException("Receiver must be the item owner.");
    }

    return messageService.createMessage(
        sender,
        receiver,
        item,
        req.getContent());
  }

  @PostMapping(value = "/items/{itemId}/messages", consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE }, produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView createMessageHtml(
      @PathVariable String itemId,
      @Valid MessagePostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal,
      Model model) {

    Message created = createMessage(itemId, req, principal.getUser());

    // Add the view model
    MessageGetResponse view = MessageGetResponse.fromMessage(created);
    model.addAttribute("message", view);

    // Return a Thymeleaf fragment
    return new ModelAndView("items/message", model.asMap());
  }

  @PostMapping(value = "/items/{itemId}/messages", consumes = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
          MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<MessageGetResponse> createMessageApi(
      @PathVariable String itemId,
      @Valid @RequestBody MessagePostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Message created = createMessage(itemId, req, principal.getUser());

    URI location = URI.create("/items/" + itemId + "/messages/" + created.getUuid());

    return ResponseEntity.created(location)
        .body(MessageGetResponse.fromMessage(created));
  }

  // =============================
  // GET MESSAGES FOR ITEM
  // =============================

  @GetMapping(value = "/items/{itemId}/messages", produces = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE })
  public Object getMessagesForItem(
      @PathVariable String itemId,
      @RequestParam(name = "user", required = false) String userId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    Item item = itemService.getById(itemId);
    User requester = principal.getUser();
    User owner = item.getUser();

    boolean isOwner = requester.getUuid().equals(owner.getUuid());

    // CASE 1 — OWNER: LIST ALL CONVERSATIONS
    if (isOwner && userId == null) {
      // Return a page of ConversationSummaryResponse
      return messageService.getConversationSummariesForItem(item, pageable);
    }

    // CASE 2 — OWNER WITH A SPECIFIC USER SELECTED
    if (isOwner && userId != null) {
      User participant = userService.getByUuid(userId);
      return messageService.getConversationForItemWithUser(item, participant, pageable);
    }

    // CASE 3 — NON-OWNER → Only show the requester's own conversation
    return messageService.getConversationForItemWithUser(item, requester, pageable);
  }

  @GetMapping(value = "/items/{itemId}/messages", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView viewMessagesForItemHtml(
      @PathVariable String itemId,
      @RequestParam(name = "user", required = false) String userId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      Model model,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    Item item = itemService.getById(itemId);
    User requester = principal.getUser();
    User owner = item.getUser();
    boolean isOwner = requester.getUuid().equals(owner.getUuid());

    model.addAttribute("item", item);
    model.addAttribute("isOwner", isOwner);

    // CASE 1 — OWNER: LIST ALL CONVERSATIONS
    if (isOwner && userId == null) {
      Page<ConversationSummaryResponse> summaries = messageService.getConversationSummariesForItem(item, pageable);

      model.addAttribute("conversations", summaries);
      return new ModelAndView("items/conversations-summary", model.asMap());
    }

    // CASE 2 — OWNER SELECTS A SPECIFIC USER
    User participant = isOwner && userId != null
        ? userService.getByUuid(userId)
        : requester;

    Page<MessageGetResponse> conversation = messageService.getConversationForItemWithUser(item, participant, pageable);

    model.addAttribute("messages", conversation);
    model.addAttribute("participant", participant);

    return new ModelAndView("items/messages", model.asMap());
  }

}
