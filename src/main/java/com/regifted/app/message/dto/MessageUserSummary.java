package com.regifted.app.message.dto;

import lombok.Data;

@Data
public class MessageUserSummary {
  private String uuid;
  private String name;

  public static MessageUserSummary fromUser(com.regifted.app.user.User user) {
    MessageUserSummary summary = new MessageUserSummary();
    summary.setUuid(user.getUuid());
    summary.setName(user.getName());
    return summary;
  }
}
