package com.regifted.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfMessagingException extends RuntimeException {
  public SelfMessagingException() {
    super("Sender cannot message themselves");
  }

  public SelfMessagingException(String senderUuid) {
    super(String.format("User %s cannot send a message to themselves", senderUuid));
  }
}
