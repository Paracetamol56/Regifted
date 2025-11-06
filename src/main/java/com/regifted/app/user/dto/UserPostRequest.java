package com.regifted.app.user.dto;

import com.regifted.app.user.User;

import lombok.Data;

@Data
public class UserPostRequest {
  private String name;
  private String email;
  private String password;
  private Boolean notification;

  public User toUser() {
    User user = new User();
    user.setName(this.name);
    user.setEmail(this.email);
    user.setPassword(this.password);
    user.setNotification(this.notification != null ? this.notification : true);
    return user;
  }
}
