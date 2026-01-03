package com.regifted.app.user.dto;

import com.regifted.app.user.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPostRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be at most 100 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  @NotNull(message = "Notification preference is required")
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
