package com.regifted.app.user.dto;

import java.time.Instant;
import java.util.Set;

import com.regifted.app.item.Item;

import lombok.Data;

@Data
public class UserPublicResponse {
  private String uuid;
  private String name;
  private Instant createdAt;
  private Set<Item> items;

  public static UserPublicResponse fromUser(com.regifted.app.user.User user) {
    UserPublicResponse response = new UserPublicResponse();
    response.setUuid(user.getUuid());
    response.setName(user.getName());
    response.setCreatedAt(user.getCreatedAt());
    response.setItems(user.getItems());
    return response;
  }
}
