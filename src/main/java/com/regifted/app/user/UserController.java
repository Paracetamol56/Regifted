package com.regifted.app.user;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService svc;

  public UserController(UserService svc) {
    this.svc = svc;
  }

  // admin-style: get user by uuid (protected)
  @GetMapping("/{uuid}")
  public User getUser(@PathVariable String uuid) {
    return svc.getByUuid(uuid);
  }
}
