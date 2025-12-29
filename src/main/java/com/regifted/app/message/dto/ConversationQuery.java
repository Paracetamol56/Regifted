package com.regifted.app.message.dto;

import com.regifted.app.item.Item;
import com.regifted.app.user.User;

public record ConversationQuery(
    Item item,
    User requester,
    User requestedParticipant) {
}
