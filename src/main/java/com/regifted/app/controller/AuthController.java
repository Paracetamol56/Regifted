package com.regifted.app.controller;

import com.regifted.app.dto.LoginRequest;
import com.regifted.app.dto.LoginResponse;
import com.regifted.app.dto.RegisterRequest;
import com.regifted.app.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService svc;

    public AuthController(UserService svc) { this.svc = svc; }

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
