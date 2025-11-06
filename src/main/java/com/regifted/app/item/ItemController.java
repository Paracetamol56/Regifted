package com.regifted.app.item;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.item.dto.ItemPostRequest;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
          MediaType.APPLICATION_XML_VALUE,
      })
  public Item createItem(ItemPostRequest req) {
    return itemService.createItem(req);
  }

  @GetMapping(produces = { MediaType.APPLICATION_XML_VALUE,
      MediaType.TEXT_HTML_VALUE })
  public Object getAll(Model model) {
    List<Item> items = itemService.getAllItems();

    model.addAttribute("items", items);
    return new ModelAndView("items");
  }

  @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public Item getItemById(@PathVariable String id) {
    return itemService.getById(id);
  }

  @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
  public Object getItemByIdHTML(@PathVariable String id, Model model) {
    Item item = itemService.getById(id);

    model.addAttribute("item", item);
    return new ModelAndView("item");
  }
}
