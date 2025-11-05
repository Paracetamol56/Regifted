package com.regifted.app.user;

import com.regifted.app.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService svc;

  public UserController(UserService svc) {
    this.svc = svc;
  }

  // returns current user (protected)
  @GetMapping("/me")
  public User me(@AuthenticationPrincipal CustomUserDetails principal) {
    return principal.getUser();
  }

  // admin-style: get user by uuid (protected)
  @GetMapping("/{uuid}")
  public User getUser(@PathVariable String uuid) {
    return svc.getByUuid(uuid);
  }
}
