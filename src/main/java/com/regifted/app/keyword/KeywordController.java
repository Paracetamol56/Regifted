package com.regifted.app.keyword;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/keywords")
public class KeywordController {

  private final KeywordService keywordService;

  public KeywordController(KeywordService keywordService) {
    this.keywordService = keywordService;
  }

  @GetMapping
  public ResponseEntity<Page<Keyword>> getKeywords(
      @RequestParam(required = false) String q,
      @PageableDefault(size = 20, sort = "name") Pageable pageable) {

    Page<Keyword> keywords;
    if (q != null && !q.trim().isEmpty()) {
      keywords = keywordService.searchKeywords(q, pageable);
    } else {
      keywords = keywordService.getAllKeywords(pageable);
    }

    return ResponseEntity.ok(keywords);
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<Keyword> getKeywordById(@PathVariable String uuid) {
    Keyword keyword = keywordService.getByUuid(uuid);
    return ResponseEntity.ok(keyword);
  }
}
