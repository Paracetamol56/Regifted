package com.regifted.app.item;

import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.bundle.Bundle;
import com.regifted.app.bundle.BundleService;
import com.regifted.app.item.dto.ItemGetResponse;
import com.regifted.app.item.dto.ItemSearchRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordService;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;
  private final KeywordService keywordService;
  private final UserService userService;
  private final BundleService bundleService;

  public ItemController(ItemService itemService, KeywordService keywordService, UserService userService, BundleService bundleService ) {
    this.itemService = itemService;
    this.keywordService = keywordService;
    this.userService = userService;
    this.bundleService = bundleService;
  }

  // ======================
  // CREATE ITEM
  // ======================

  // HTML Form
  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public String createItemHTML(@Valid ItemPostRequest req,  @AuthenticationPrincipal CustomUserPrincipal principal) {
    User currentUser = this.userService.getByEmail(principal.getUsername());
    Item created = itemService.createItem(req, currentUser);
    return "redirect:/items/" + created.getUuid();
  }

  // JSON
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Item createItemJson(@RequestBody @Valid ItemPostRequest req,  @AuthenticationPrincipal CustomUserPrincipal principal) {
    User currentUser = this.userService.getByEmail(principal.getUsername());
    return itemService.createItem(req, currentUser);
  }

  // ======================
  // GET ALL ITEMS
  // ======================

  @GetMapping(value = "", produces = MediaType.TEXT_HTML_VALUE)
  @Transactional(readOnly = true)
  public ModelAndView getPageHtml(
          @Valid @ModelAttribute ItemSearchRequest req, 
          @AuthenticationPrincipal UserDetails userDetails,
          Model model) {
      
      Page<Item> items = itemService.getItemSearchPage(
              req.getPage(),
              req.getLimit(),
              req.getQ(),
              req.getKeyword(),
              null,
              null);
      
      List<Keyword> keywords = keywordService.getMostUsedKeywords(10);
      
      if (userDetails != null) {
          Bundle cart = bundleService.getCurrentCart(userDetails.getUsername());
          model.addAttribute("bundle", cart);
      }

      model.addAttribute("items", items.getContent());
      model.addAttribute("keywords", keywords);
      model.addAttribute("page", req.getPage());
      model.addAttribute("limit", req.getLimit());
      model.addAttribute("query", req.getQ());
      model.addAttribute("keyword", req.getKeyword());
      model.addAttribute("totalItems", items.getTotalElements());
      model.addAttribute("totalPages", items.getTotalPages());
      
      return new ModelAndView("items/index");
  }

  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  @ResponseBody
  public Page<ItemGetResponse> getPageJson(
      @Valid @ModelAttribute ItemSearchRequest req,
      @AuthenticationPrincipal UserDetails userDetails) {

      Page<Item> items = itemService.getItemSearchPage(
          req.getPage(),
          req.getLimit(),
          req.getQ(),
          req.getKeyword(),
          null,
          null);

      Bundle cart = (userDetails != null) ? bundleService.getCurrentCart(userDetails.getUsername()) : null;


      return items.map(item -> ItemGetResponse.from(item, cart));
  }

  // ======================
  // GET ITEM BY ID
  // ======================

  // JSON / XML
  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @Transactional(readOnly = true)
  @ResponseBody
  public ItemGetResponse getItemById(@PathVariable String uuid) {
    Item item = itemService.getById(uuid);
    return ItemGetResponse.from(item);
  }

  // HTML
  @GetMapping(value = "/{uuid}", produces = MediaType.TEXT_HTML_VALUE)
  @Transactional(readOnly = true)
  public ModelAndView getItemByIdHTML(
      @PathVariable String uuid,
      @AuthenticationPrincipal CustomUserPrincipal principal,
      Model model) {

    Item item = itemService.getById(uuid);

    if (principal == null) {
      model.addAttribute("item", item);
      model.addAttribute("isOwner", false);
      model.addAttribute("liked", false);
      return new ModelAndView("items/{uuid}");
    }

    User currentUser = userService.getByEmail(principal.getUsername());
    boolean liked = userService.hasLikedItem(currentUser, item);

    model.addAttribute("item", item);
    model.addAttribute("isOwner", currentUser.equals(item.getUser()));
    model.addAttribute("liked", liked);
    return new ModelAndView("items/{uuid}");
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
  public ItemGetResponse updateItemJson(@PathVariable String uuid, @RequestBody @Valid ItemPutRequest req) {
    Item item = itemService.updateItemById(uuid, req);
    return ItemGetResponse.from(item);
  }

  // ======================
  // DELETE ITEM
  // ======================

  @DeleteMapping("/{uuid}")
  public ResponseEntity<Void> deleteItem(@PathVariable String uuid) {
    itemService.deleteItemById(uuid);

    HttpHeaders headers = new HttpHeaders();
    headers.add("HX-Redirect", "/items");

    return ResponseEntity.ok().headers(headers).build();
  }
}
