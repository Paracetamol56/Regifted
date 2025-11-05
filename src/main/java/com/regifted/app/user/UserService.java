package com.regifted.app.user;

@org.springframework.stereotype.Service
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repo) {
    this.repository = repo;
  }

  public User getByUuid(String uuid) {
    return repository.findById(uuid).orElse(null);
  }
}
