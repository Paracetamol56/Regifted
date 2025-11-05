package com.regifted.app.user;

import com.regifted.app.security.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

@org.springframework.stereotype.Service
public class UserService {

  private final UserRepository repository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;

  public UserService(UserRepository repo, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
    this.repository = repo;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
  }

  public User getByUuid(String uuid) {
    return repository.findById(uuid).orElse(null);
  }
}
