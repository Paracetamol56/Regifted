package com.regifted.app.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.regifted.app.user.dto.UserPostRequest;

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
  public User createUser(UserPostRequest req) {
    return svc.createUser(req);
  }

  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public User getUser(@PathVariable String uuid) {
    return svc.getByUuid(uuid);
  }
}
