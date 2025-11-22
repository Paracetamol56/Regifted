package com.regifted.app.item;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.item.dto.ItemSearchRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordService;
import com.regifted.app.user.UserService;
import com.regifted.app.user.User;

import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;
  private final KeywordService keywordService;
  private final UserService userService;

  public ItemController(ItemService itemService, KeywordService keywordService, UserService userService) {
    this.itemService = itemService;
    this.keywordService = keywordService;
    this.userService = userService;
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
  @GetMapping(value = "", produces = MediaType.TEXT_HTML_VALUE)
  @Transactional(readOnly = true)
  @ResponseBody
  public ModelAndView getPageHtml(@Valid @ModelAttribute ItemSearchRequest req, Model model) {
    Page<Item> items = itemService.getItemSearchPage(req.getPage(), req.getLimit(), req.getQ(), req.getKeyword());
    List<Keyword> keywords = keywordService.getMostUsedKeywords(10);
    model.addAttribute("items", items.getContent());
    model.addAttribute("keywords", keywords);
    model.addAttribute("page", req.getPage());
    model.addAttribute("limit", req.getLimit());
    model.addAttribute("query", req.getQ());
    model.addAttribute("totalItems", items.getTotalElements());
    model.addAttribute("totalPages", items.getTotalPages());
    return new ModelAndView("items");
  }

  // JSON
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  @ResponseBody
  public List<Item> getPageJson(
      @Valid @ModelAttribute ItemSearchRequest req) {

    return itemService.getItemSearchPage(req.getPage(), req.getLimit(), req.getQ(), req.getKeyword()).getContent();
  }

  // ======================
  // GET ITEM BY ID
  // ======================

  // JSON / XML
  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @Transactional(readOnly = true)
  @ResponseBody
  public Item getItemById(@PathVariable String uuid) {
    return itemService.getById(uuid);
  }

  // HTML
  @GetMapping(value = "/{uuid}", produces = MediaType.TEXT_HTML_VALUE)
  @Transactional(readOnly = true)
  public ModelAndView getItemByIdHTML(@PathVariable String uuid, @AuthenticationPrincipal UserDetails principal,
      Model model) {
    Item item = itemService.getById(uuid);
    if (principal == null) {
      model.addAttribute("item", item);
      model.addAttribute("favorited", false);
      return new ModelAndView("item");
    }
    User currentUser = this.userService.getByEmail(principal.getUsername());
    boolean favorited = currentUser != null && currentUser.getFavoriteItems().contains(item);

    model.addAttribute("item", item);
    model.addAttribute("currentUser", currentUser);
    model.addAttribute("favorited", favorited);
    return new ModelAndView("item");
  }

  // ======================
  // UPDATE ITEM
  // ======================

  // Form HTML
  @GetMapping(value = "/update/{uuid}", produces = MediaType.TEXT_HTML_VALUE)
  public String getUpdateForm(@PathVariable String uuid, Model model) {
    Item item = itemService.getById(uuid);
    model.addAttribute("item", item);
    return "update-item";
  }

  @PutMapping(value = "/{uuid}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String updateItemHTML(@PathVariable String uuid, @Valid ItemPutRequest req) {
    Item updated = itemService.updateItemById(uuid, req);
    return "redirect:/items/" + updated.getUuid();
  }

  // JSON
  @PutMapping(value = "/{uuid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Item updateItemJson(@PathVariable String uuid, @RequestBody @Valid ItemPutRequest req) {
    return itemService.updateItemById(uuid, req);
  }
}
