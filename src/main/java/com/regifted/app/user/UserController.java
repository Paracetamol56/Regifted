package com.regifted.app.user;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.user.dto.UserPostRequest;
import com.regifted.app.user.dto.UserPublicResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService svc) {
    this.userService = svc;
  }

  @PostMapping(value = "", produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE
  }, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      MediaType.APPLICATION_JSON_VALUE
  })
  public User createUser(@RequestBody @Valid UserPostRequest req) {
    return userService.createUser(req);
  }

  @GetMapping(value = "/me", produces = { MediaType.TEXT_HTML_VALUE })
  public ModelAndView getMe(@AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) {
      return new ModelAndView("redirect:/login");
    }

    User user = userService.getByEmail(principal.getUsername());
    model.addAttribute("user", user);

    return new ModelAndView("users/me");
  }

  @GetMapping(value = "/me", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public User getMe(@AuthenticationPrincipal UserDetails principal) {
    return userService.getByEmail(principal.getUsername());
  }

  @GetMapping(value = "/{uuid}", produces = { MediaType.TEXT_HTML_VALUE })
  public ModelAndView getUser(@PathVariable String uuid, Model model) {
    User user = userService.getByUuid(uuid);
    model.addAttribute("user", user);

    return new ModelAndView("users/{uuid}");
  }

  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public UserPublicResponse getUser(@PathVariable String uuid) {
    UserPublicResponse res = UserPublicResponse.fromUser(userService.getByUuid(uuid));

    return res;
  }

  @PatchMapping(value = "favorite/{itemUuid}/{userUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> toggleFavorite(
      @PathVariable String itemUuid,
      @PathVariable String userUuid) {
    System.out.println("Received toggle favorite request for user " + userUuid + " and item " + itemUuid);
    boolean result = userService.toggleFavorite(itemUuid, userUuid);
    return ResponseEntity.ok(result);
  }

}
