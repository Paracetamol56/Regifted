
package com.regifted.app.item;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regifted.app.item.dto.ItemPostRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE })
  public ResponseEntity<?> createItem(
      HttpServletRequest request,
      @RequestHeader(name = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String accept)
      throws IOException {
    String contentType = request.getContentType();

    ItemPostRequest req;

    if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
      // Parse JSON manually
      String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
      req = new ObjectMapper().readValue(body, ItemPostRequest.class);
    } else {
      // Handle form data
      req = new ItemPostRequest();
    }

    Item created = itemService.createItem(req);
    URI location = URI.create(request.getRequestURL().toString() + "/" + created.getUuid());

    if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
      return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    return ResponseEntity.created(location).body(created);
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
