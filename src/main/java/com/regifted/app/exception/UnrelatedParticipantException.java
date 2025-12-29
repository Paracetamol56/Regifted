package com.regifted.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnrelatedParticipantException extends RuntimeException {
  public UnrelatedParticipantException() {
    super("At least one participant must be related to the item");
  }

  public UnrelatedParticipantException(String itemId, String senderUuid, String receiverUuid) {
    super(String.format(
        "Neither sender (%s) nor receiver (%s) are related to item %s",
        senderUuid, receiverUuid, itemId));
  }
}
