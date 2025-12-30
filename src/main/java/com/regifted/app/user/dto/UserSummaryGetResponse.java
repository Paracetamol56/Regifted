package com.regifted.app.user.dto;

import java.time.Instant;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.user.User;

import lombok.Data;

@Data
public class UserSummaryGetResponse {
  protected String uuid;
  protected String href;
  protected String name;

  public static UserSummaryGetResponse from(User user) {
    UserSummaryGetResponse response = new UserSummaryGetResponse();
    response.setUuid(user.getUuid());
    response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/users/{uuid}")
        .buildAndExpand(user.getUuid()) // Changed from 'keyword.getUuid()'
        .toUriString());
    response.setName(user.getName());
    return response;
  }

}
