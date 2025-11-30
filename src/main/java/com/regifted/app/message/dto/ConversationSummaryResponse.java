package com.regifted.app.message.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversationSummaryResponse {
  private String participantName;
  private String participantUuid;
  private String itemTitle;
  private String itemUuid;
  private String lastMessageSnippet;
  private Instant lastMessageTimestamp;
}
