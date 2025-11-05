
package com.regifted.app.item;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
      MediaType.TEXT_HTML_VALUE })
  public Object getAll(
      @RequestHeader(name = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept,
      Model model) {
    List<Item> items = itemService.getAllItems();

    if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
      model.addAttribute("items", items);
      return new ModelAndView("items"); // templates/items.html
    }

    return items;
  }

  @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
      MediaType.TEXT_HTML_VALUE })
  public Object getOne(
      @PathVariable String id,
      @RequestHeader(name = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept,
      Model model) {
    Item item = itemService.getById(id);

    if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
      model.addAttribute("item", item);
      return new ModelAndView("item"); // templates/item.html
    }

    return item;
  }
}
