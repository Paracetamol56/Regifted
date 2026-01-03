package com.regifted.app.user;

import com.regifted.app.item.Item;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.dto.UserPostRequest;
import com.regifted.app.user.dto.UserPrivateGetResponse;
import com.regifted.app.user.dto.UserPublicGetResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService svc) {
    this.userService = svc;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE })
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserPrivateGetResponse createUserApi(@Valid @RequestBody UserPostRequest req) {
    User user = userService.createUser(req);
    return UserPrivateGetResponse.from(user);
  }

  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView registerFromWeb(@Valid @ModelAttribute UserPostRequest req, Model model) {
    User user = userService.createUser(req);
    model.addAttribute("user", user);
    return new ModelAndView("users/me");
  }

  @GetMapping(value = "/me", produces = { MediaType.TEXT_HTML_VALUE })
  public ModelAndView getMe(@AuthenticationPrincipal CustomUserPrincipal principal, Model model) {
    if (principal == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authentified");

    User user = userService.getByEmail(principal.getUsername());
    model.addAttribute("user", user);

    return new ModelAndView("users/me");
  }

  @GetMapping(value = "/me", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public ResponseEntity<UserPrivateGetResponse> getMeApi(@AuthenticationPrincipal CustomUserPrincipal principal) {
    if (principal == null)
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    User user = userService.getByEmail(principal.getUsername());
    return ResponseEntity.ok(UserPrivateGetResponse.from(user));
  }

  @GetMapping(value = "/{uuid}", produces = { MediaType.TEXT_HTML_VALUE })
  public ModelAndView getUserHtml(@PathVariable String uuid, Model model) {
    User user = userService.getByUuid(uuid);
    model.addAttribute("user", user);

    return new ModelAndView("users/{uuid}");
  }

  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public UserPublicGetResponse getUserApi(@PathVariable String uuid) {
    User user = userService.getByUuid(uuid);
    return UserPublicGetResponse.from(user);
  }

  @PostMapping(value = "/me/likes", produces = MediaType.APPLICATION_JSON_VALUE)
  public ModelAndView addLike(@RequestParam("item") String itemUuid,
      @AuthenticationPrincipal CustomUserPrincipal principal, Model model) {
    // We need a full refresh of the user to get the liked items (lazy loaded)
    User currentUser = userService.getByEmail(principal.getUsername());
    System.out.println("Adding like for user: " + currentUser);
    Item item = userService.addLike(currentUser, itemUuid);

    model.addAttribute("liked", true);
    model.addAttribute("item", item);

    return new ModelAndView("items/like-button");
  }

  @DeleteMapping(value = "/me/likes", produces = MediaType.APPLICATION_JSON_VALUE)
  public ModelAndView removeLike(@RequestParam("item") String itemUuid,
      @AuthenticationPrincipal CustomUserPrincipal principal, Model model) {
    // We need a full refresh of the user to get the liked items (lazy loaded)
    User currentUser = userService.getByEmail(principal.getUsername());
    Item item = userService.removeLike(currentUser, itemUuid);

    model.addAttribute("liked", false);
    model.addAttribute("item", item);

    return new ModelAndView("items/like-button");
  }
}
