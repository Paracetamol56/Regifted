package com.regifted.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStatusTransitionException extends RuntimeException {
  public InvalidStatusTransitionException(String message) {
    super(message);
  }

  public static InvalidStatusTransitionException cannotSetToDraft() {
    return new InvalidStatusTransitionException("Cannot change status to DRAFT");
  }

  public static InvalidStatusTransitionException onlySentCanBeAcceptedOrRefused() {
    return new InvalidStatusTransitionException("Only SENT carts can be accepted or refused");
  }

  public static InvalidStatusTransitionException onlyDraftCanBeSent() {
    return new InvalidStatusTransitionException("Only DRAFT carts can be sent");
  }
}
