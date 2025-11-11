package com.regifted.app.item;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.item.dto.ItemPostRequest;

import java.net.URI;
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
          MediaType.APPLICATION_XML_VALUE })
  public String createItem(ItemPostRequest req) {
    Item created = itemService.createItem(req);
    return "redirect:/items/" + created.getUuid();
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

  @GetMapping(value = "/update/{id}", produces = MediaType.TEXT_HTML_VALUE)
  public Object getUpdateByIdHTML(@PathVariable String id, Model model) {
    Item item = itemService.getById(id);

    model.addAttribute("item", item);
    return new ModelAndView("update-item");
  }

  @PutMapping(value = "/update/{id}", 
    consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE }, 
    produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public String updateItem(@PathVariable String id, @ModelAttribute ItemPostRequest req) {
      System.out.println("Received update request for ID: " + id + " with data: " + req);
      Item updated = itemService.updateItembyId(id, req);
      return "redirect:/items/" + updated.getUuid();
  }
}
