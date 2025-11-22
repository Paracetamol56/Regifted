package com.regifted.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
  public NotFoundException(String id) {
    super("Not found with id: " + id);
  }
}
