package com.regifted.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CartStatusPermissionException extends RuntimeException {
  public CartStatusPermissionException(String message) {
    super(message);
  }

  public static CartStatusPermissionException onlyUserCanSend() {
    return new CartStatusPermissionException("Only the cart user can send it");
  }

  public static CartStatusPermissionException onlyOwnerCanAcceptOrRefuse() {
    return new CartStatusPermissionException("Only the cart owner can accept or refuse it");
  }
}
