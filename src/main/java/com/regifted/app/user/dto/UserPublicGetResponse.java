package com.regifted.app.user.dto;

import java.time.Instant;
import java.util.Set;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.item.dto.ItemGetResponse;
import com.regifted.app.user.User;

import lombok.Data;

@Data
public class UserPublicGetResponse {
  protected String uuid;
  protected String href;
  protected String name;
  protected Instant createdAt;
  protected Set<ItemGetResponse> items;

  public static UserPublicGetResponse from(User user) {
    UserPublicGetResponse response = new UserPublicGetResponse();
    response.setUuid(user.getUuid());
    response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/users/{uuid}")
        .buildAndExpand(user.getUuid()) // Changed from 'keyword.getUuid()'
        .toUriString());
    response.setName(user.getName());
    response.setCreatedAt(user.getCreatedAt());
    response.setItems(user.getItems().stream()
        .map(ItemGetResponse::from)
        .collect(java.util.stream.Collectors.toSet()));
    return response;
  }

}
