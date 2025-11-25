package com.regifted.app.message.dto;

import lombok.Data;

@Data
public class MessageGetResponse {
  private String uuid;
  private String content;
  private String senderName;
  private String senderUuid;
  private String receiverName;
  private String receiverUuid;
  private String itemUuid;
}
