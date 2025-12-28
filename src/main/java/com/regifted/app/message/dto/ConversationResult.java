package com.regifted.app.message.dto;

import org.springframework.data.domain.Page;

import com.regifted.app.user.User;

public sealed interface ConversationResult
    permits ConversationResult.Conversations, ConversationResult.Messages {

  record Conversations(Page<ConversationSummaryResponse> page)
      implements ConversationResult {
  }

  record Messages(Page<MessageGetResponse> page, User participant)
      implements ConversationResult {
  }

  static ConversationResult conversations(Page<ConversationSummaryResponse> page) {
    return new Conversations(page);
  }

  static ConversationResult messages(Page<MessageGetResponse> page, User participant) {
    return new Messages(page, participant);
  }
}
