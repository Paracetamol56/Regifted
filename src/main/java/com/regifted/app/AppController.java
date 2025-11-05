package com.regifted.app;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppController {

  public AppController() {
  }

  @GetMapping("/health")
  public String health() {
    return "OK";
  }
}
