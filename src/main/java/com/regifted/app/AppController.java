package com.regifted.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AppController {

  public AppController() {
  }

  @GetMapping("/")
  public String home() {
    return "index.html";
  }

  @GetMapping("/register")
  public String register() {
    return "register";
  }

  @GetMapping("/new-item")
  public String newItem() {
    return "new-item";
  }
}
