package com.regifted.app;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.item.ItemService;
import com.regifted.app.item.Item;

@Controller
public class AppController {

  private ItemService itemService;

  public AppController(ItemService itemService) {
    this.itemService = itemService;
  }

  @GetMapping("/")
  public ModelAndView home(Model model) {
    Page<Item> lastAddedItems = itemService.getItemSearchPage(0, 3, null, null, "createdAt", "desc");
    model.addAttribute("lastAddedItems", lastAddedItems);

    return new ModelAndView("index");
  }

  @GetMapping("/register")
  public String register() {
    return "register";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/new-item")
  public String newItem() {
    return "new-item";
  }
}
