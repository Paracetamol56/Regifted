package com.regifted.app.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

import com.regifted.app.auth.dto.LoginRequest;
import com.regifted.app.auth.dto.LoginResponse;
import com.regifted.app.auth.dto.RegisterRequest;
import com.regifted.app.security.JwtUtils;
import com.regifted.app.user.User;
import com.regifted.app.user.UserRepository;

@org.springframework.stereotype.Service
class AuthService {

  private final UserRepository repository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;

  public AuthService(UserRepository repo, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
    this.repository = repo;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
  }

  public LoginResponse register(RegisterRequest req) {
    // Check if request validity
    Assert.hasText(req.getName(), "Name is required");
    Assert.hasText(req.getEmail(), "Email is required");
    Assert.isTrue(
        req.getEmail().matches(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"),
        "Invalid email format");
    Assert.hasText(req.getPassword(), "Password is required");
    // Check if email already exists
    if (repository.existsByEmail(req.getEmail())) {
      throw new IllegalArgumentException("Email already in use");
    }
    User u = new User();
    u.setName(req.getName());
    u.setEmail(req.getEmail());
    u.setPassword(passwordEncoder.encode(req.getPassword()));
    repository.save(u);
    String token = jwtUtils.generateToken(u.getUuid(), u.getEmail());
    return new LoginResponse(token);
  }

  public LoginResponse login(LoginRequest req) {
    // Check if request validity
    Assert.hasText(req.getEmail(), "Email is required");
    Assert.hasText(req.getPassword(), "Password is required");
    User u = repository.findByEmail(req.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    String token = jwtUtils.generateToken(u.getUuid(), u.getEmail());
    return new LoginResponse(token);
  }
}
