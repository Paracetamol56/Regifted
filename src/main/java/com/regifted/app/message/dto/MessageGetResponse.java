package com.regifted.app.message.dto;

import java.time.Instant;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.message.Message;
import com.regifted.app.user.dto.UserSummaryGetResponse;

import lombok.Data;

@Data
public class MessageGetResponse {
  private String uuid;
  private String href;
  private String content;
  private Instant createdAt;
  private UserSummaryGetResponse sender;
  private UserSummaryGetResponse receiver;

  public static MessageGetResponse from(Message message) {
    MessageGetResponse response = new MessageGetResponse();
    response.setUuid(message.getUuid());
    response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/items/{itemUuid}/messages/{messageUuid}")
        .buildAndExpand(message.getItem().getUuid(), message.getUuid())
        .toUriString());
    response.setContent(message.getContent());
    response.setCreatedAt(message.getCreatedAt());
    response.setSender(UserSummaryGetResponse.from(message.getSender()));
    response.setReceiver(UserSummaryGetResponse.from(message.getReceiver()));
    return response;
  }
}
