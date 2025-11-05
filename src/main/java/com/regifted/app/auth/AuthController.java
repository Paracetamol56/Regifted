package com.regifted.app.auth;

import com.regifted.app.auth.dto.LoginRequest;
import com.regifted.app.auth.dto.LoginResponse;
import com.regifted.app.auth.dto.RegisterRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService svc;

  public AuthController(AuthService svc) {
    this.svc = svc;
  }

  @PostMapping("/register")
  public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
    var resp = svc.register(req);
    return ResponseEntity.created(null).body(resp);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    var resp = svc.login(req);
    return ResponseEntity.ok(resp);
  }
}
