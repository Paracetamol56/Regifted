package com.regifted.app.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
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

import com.regifted.app.message.dto.MessagePostRequest;
import com.regifted.app.security.CustomUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/messages")
public class MessageController {

  private final MessageService messageService;

  public MessageController(MessageService svc) {
    this.messageService = svc;
  }

  // -----------------------------
  // JSON / XML LIST
  // -----------------------------
  @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public Page<Message> getMessages(
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    return messageService.getMessages(principal.getUser(), pageable);
  }

  // -----------------------------
  // HTML LIST
  // -----------------------------
  @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView getMessagesHtml(
      Model model,
      @RequestParam(required = false) String itemId,
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    model.addAttribute("page", messageService.getMessages(principal.getUser(), pageable));
    return new ModelAndView("messages/index", model.asMap());
  }

  // -----------------------------
  // LAST MESSAGE PER ITEM (JSON / XML)
  // -----------------------------
  @GetMapping(value = "/last", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public Page<Message> getLastMessagesForUser(
      @RequestParam String userId,
      @PageableDefault(size = 20) Pageable pageable,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    return messageService.getLastMessagesForUser(principal.getUser(), pageable);
  }

  // -----------------------------
  // LAST MESSAGE PER ITEM (HTML)
  // -----------------------------
  @GetMapping(value = "/last", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView getLastMessagesForUserHtml(
      @RequestParam String userId,
      @PageableDefault(size = 20) Pageable pageable,
      Model model,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    model.addAttribute("page", messageService.getLastMessagesForUser(principal.getUser(), pageable));
    return new ModelAndView("messages/last", model.asMap());
  }

  // -----------------------------
  // GET BY ID (HTML)
  // -----------------------------
  @GetMapping(value = "/{uuid}", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView getMessage(@PathVariable String uuid, Model model) {
    model.addAttribute("message", messageService.getById(uuid));
    return new ModelAndView("messages/detail", model.asMap());
  }

  // -----------------------------
  // CREATE MESSAGE
  // -----------------------------
  @PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE })
  public Message createMessage(@RequestBody @Valid MessagePostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    return messageService.createMessage(principal.getUser(), req);
  }
}
