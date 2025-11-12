package com.regifted.app.item;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  /**
 * 
 * POST ITEMS 
 * 
 */

  @PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE }, produces = {  MediaType.APPLICATION_XML_VALUE })
  public String createItem(ItemPostRequest req) {
    Item created = itemService.createItem(req);
    return "redirect:/items/" + created.getUuid();
  }

  @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE } )
  @ResponseBody  
  public Item createItemJson(@RequestBody ItemPostRequest req) {
      Item created = itemService.createItem(req);
      return created;
  }

/**
 * 
 * GET ITEMS 
 * 
 */

// Retour HTML ou XML
@GetMapping(produces = { MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_XML_VALUE })
public ModelAndView getAllHtml(Model model) {
    List<Item> items = itemService.getAllItems();
    model.addAttribute("items", items);
    return new ModelAndView("items");
}

// Retour JSON
@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
public List<Item> getAllJson() {
    return itemService.getAllItems();
}


/**
 * 
 * GET ITEM BY ID
 * 
 */


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


  /**
 * 
 * UPDATE ITEMS 
 * 
 */


  @GetMapping(value = "/update/{id}", produces = MediaType.TEXT_HTML_VALUE)
  public Object getUpdateByIdHTML(@PathVariable String id, Model model) {
    Item item = itemService.getById(id);

    model.addAttribute("item", item);
    return new ModelAndView("update-item");
  }

  @PutMapping(value = "/update/{id}", 
    consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE}, 
    produces = { MediaType.APPLICATION_XML_VALUE })
  public String updateItemHTML(@PathVariable String id, @ModelAttribute ItemPostRequest req) {
      System.out.println("Received update request for ID: " + id + " with data: " + req);
      Item updated = itemService.updateItembyId(id, req);
      return "redirect:/items/" + updated.getUuid();
  }

  @PutMapping(value = "/update/{id}", 
    consumes = { MediaType.APPLICATION_JSON_VALUE }, 
    produces = { MediaType.APPLICATION_JSON_VALUE })
  @ResponseBody
  public Item updateItemJson(@PathVariable String id, @RequestBody ItemPostRequest req) {
    Item updated = itemService.updateItembyId(id, req);
    return updated;
  }
}
