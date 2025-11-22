package com.regifted.app.user;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.user.dto.UserPostRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService svc;

  public UserController(UserService svc) {
    this.svc = svc;
  }

  @PostMapping(value = "", produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE
  }, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      MediaType.APPLICATION_JSON_VALUE
  })
  public User createUser(@RequestBody @Valid UserPostRequest req) {
    return svc.createUser(req);
  }

  @GetMapping(value = "/me", produces = { MediaType.TEXT_HTML_VALUE })
  public ModelAndView getMe(@AuthenticationPrincipal UserDetails principal, Model model) {
    if (principal == null) {
      return new ModelAndView("redirect:/login");
    }

    User user = svc.getByEmail(principal.getUsername());
    model.addAttribute("user", user);

    return new ModelAndView("users/me");
  }

  @GetMapping(value = "/me", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public User getMe(@AuthenticationPrincipal UserDetails principal) {
    return svc.getByEmail(principal.getUsername());
  }

  @GetMapping(value = "/{uuid}", produces = { MediaType.TEXT_HTML_VALUE })
  public ModelAndView getUser(@PathVariable String uuid, Model model) {
    User user = svc.getByUuid(uuid);
    model.addAttribute("user", user);

    return new ModelAndView("users/{uuid}");
  }

  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public User getUser(@PathVariable String uuid) {
    return svc.getByUuid(uuid);
  }
}
