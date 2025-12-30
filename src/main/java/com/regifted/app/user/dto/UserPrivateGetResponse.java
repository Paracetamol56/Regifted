package com.regifted.app.user.dto;

import java.time.Instant;
import java.util.Set;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.item.dto.ItemGetResponse;
import com.regifted.app.user.User;

import lombok.Data;

@Data
public final class UserPrivateGetResponse {
  protected String uuid;
  protected String href;
  protected String name;
  protected Instant createdAt;
  protected Set<ItemGetResponse> items;
  private String email;
  private Boolean notification;
  private Instant updatedAt;

  public static UserPrivateGetResponse from(User user) {
    UserPrivateGetResponse response = new UserPrivateGetResponse();
    response.setUuid(user.getUuid());
    response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/users/{uuid}")
        .buildAndExpand(user.getUuid())
        .toUriString());
    response.setName(user.getName());
    response.setCreatedAt(user.getCreatedAt());
    response.setItems(user.getItems().stream()
        .map(ItemGetResponse::from)
        .collect(java.util.stream.Collectors.toSet()));
    response.setEmail(user.getEmail());
    response.setNotification(user.isNotification());
    response.setUpdatedAt(user.getUpdatedAt());
    return response;
  }

}
