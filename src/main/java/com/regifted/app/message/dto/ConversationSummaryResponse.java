package com.regifted.app.message.dto;

import lombok.Data;

@Data
public class ConversationSummaryResponse {
  private String participantName;
  private String itemTitle;
  private String itemUuid;
  private String lastMessageSnippet;
  private String lastMessageTimestamp;
}
