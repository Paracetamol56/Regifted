package com.regifted.app.message.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class MessageGetResponse {
  private String uuid;
  private String content;
  private Instant createdAt;
  private MessageUserSummary sender;
  private MessageUserSummary receiver;

  public static MessageGetResponse fromMessage(com.regifted.app.message.Message message) {
    MessageGetResponse response = new MessageGetResponse();
    response.setUuid(message.getUuid());
    response.setContent(message.getContent());
    response.setCreatedAt(message.getCreatedAt());
    response.setSender(MessageUserSummary.fromUser(message.getSender()));
    response.setReceiver(MessageUserSummary.fromUser(message.getReceiver()));
    return response;
  }
}
