package com.regifted.app;

import org.springframework.web.bind.annotation.*;

@RestController
public class AppController {

  public AppController() {
  }

  @GetMapping("/")
  public String index() {
    return "Welcome to ReGifted";
  }
}
