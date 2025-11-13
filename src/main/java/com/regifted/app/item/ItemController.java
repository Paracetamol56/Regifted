package com.regifted.app.item;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;

import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  // ======================
  // CREATE ITEM
  // ======================

  // HTML Form
  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String createItemHTML(@Valid ItemPostRequest req) {
    Item created = itemService.createItem(req);
    return "redirect:/items/" + created.getUuid();
  }

  // JSON
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Item createItemJson(@RequestBody @Valid ItemPostRequest req) {
    return itemService.createItem(req);
  }

  // ======================
  // GET ALL ITEMS
  // ======================

  // HTML
  @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
  public String getAllHtml(Model model) {
    List<Item> items = itemService.getAllItems();
    model.addAttribute("items", items);
    return "items";
  }

  // JSON
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<Item> getAllJson() {
    return itemService.getAllItems();
  }

  // ======================
  // GET ITEM BY ID
  // ======================

  // JSON / XML
  @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public Item getItemById(@PathVariable String id) {
    return itemService.getById(id);
  }

  // HTML
  @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
  public String getItemByIdHTML(@PathVariable String id, Model model) {
    Item item = itemService.getById(id);
    model.addAttribute("item", item);
    return "item";
  }

  // ======================
  // UPDATE ITEM
  // ======================

  // Form HTML
  @GetMapping(value = "/update/{id}", produces = MediaType.TEXT_HTML_VALUE)
  public String getUpdateForm(@PathVariable String id, Model model) {
    Item item = itemService.getById(id);
    model.addAttribute("item", item);
    return "update-item"; // template Thymeleaf
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String updateItemHTML(@PathVariable String id, @Valid ItemPutRequest req) {
    Item updated = itemService.updateItemById(id, req);
    return "redirect:/items/" + updated.getUuid();
  }

  // JSON
  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Item updateItemJson(@PathVariable String id, @RequestBody @Valid ItemPutRequest req) {
    return itemService.updateItemById(id, req);
  }
}
