package com.regifted.app.user;

import com.regifted.app.user.dto.UserPostRequest;

@org.springframework.stereotype.Service
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repo) {
    this.repository = repo;
  }

  public User createUser(UserPostRequest req) {
    return repository.save(req.toUser());
  }

  public User getByUuid(String uuid) {
    return repository.findById(uuid).orElse(null);
  }
}
