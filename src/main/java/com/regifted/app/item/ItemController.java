package com.regifted.app.item;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordService;

import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemController {

  private final ItemService itemService;
  private final KeywordService keywordService;

  public ItemController(ItemService itemService, KeywordService keywordService) {
    this.itemService = itemService;
    this.keywordService = keywordService;
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
  public ModelAndView getPageHtml(
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "keyword", required = false) String keyword,
      Model model) {
    Page<Item> items = itemService.getItemSearchPage(page, limit, q, keyword);
    List<Keyword> keywords = keywordService.getMostUsedKeywords(10);
    System.out.println("KEYWORDS: " + keywords);
    model.addAttribute("items", items.getContent());
    model.addAttribute("keywords", keywords);
    model.addAttribute("page", page);
    model.addAttribute("limit", limit);
    model.addAttribute("query", q);
    model.addAttribute("totalItems", items.getTotalElements());
    model.addAttribute("totalPages", items.getTotalPages());
    return new ModelAndView("items");
  }

  // JSON
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  @ResponseBody
  public List<Item> getPageJson(
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "keyword", required = false) String keyword) {

    return itemService.getItemSearchPage(page, limit, q, keyword).getContent();
  }

  // ======================
  // GET ITEM BY ID
  // ======================

  // JSON / XML
  @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @Transactional(readOnly = true)
  @ResponseBody
  public Item getItemById(@PathVariable String id) {
    return itemService.getById(id);
  }

  // HTML
  @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
  @Transactional(readOnly = true)
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
    return "update-item";
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
