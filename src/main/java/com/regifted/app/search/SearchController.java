package com.regifted.app.search;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.regifted.app.search.dto.SearchGetResponse;
import com.regifted.app.search.dto.SearchPostRequest;
import com.regifted.app.security.CustomUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/searches")
public class SearchController {

  private final SearchService searchService;

  public SearchController(SearchService searchService) {
    this.searchService = searchService;
  }

  // =============================
  // CREATE SEARCH
  // =============================

  @PostMapping(value = "", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
      MediaType.APPLICATION_FORM_URLENCODED_VALUE }, produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView createSearchHtml(
      @Valid SearchPostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal,
      Model model) {

    Search created = searchService.createSearch(req.getQuery(), principal.getUser());

    model.addAttribute("search", SearchGetResponse.fromSearch(created));

    return new ModelAndView("searches/search", model.asMap());
  }

  @PostMapping(value = "", consumes = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
          MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<SearchGetResponse> createSearchApi(
      @Valid @RequestBody SearchPostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Search created = searchService.createSearch(req.getQuery(), principal.getUser());

    URI location = URI.create("/searches/" + created.getUuid());

    return ResponseEntity.created(location)
        .body(SearchGetResponse.fromSearch(created));
  }

  // =============================
  // GET SEARCHES
  // =============================

  @GetMapping(value = "", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public Page<SearchGetResponse> getSearchesApi(
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Page<Search> searches = searchService.getAllSearchesForUser(principal.getUser(), pageable);

    return searches.map(SearchGetResponse::fromSearch);
  }

  @GetMapping(value = "", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView getSearchesHtml(
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      Model model,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Page<Search> searches = searchService.getAllSearchesForUser(principal.getUser(), pageable);

    model.addAttribute("searches", searches);

    return new ModelAndView("searches/list", model.asMap());
  }

  // =============================
  // GET SEARCH BY UUID
  // =============================

  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE })
  public SearchGetResponse getSearchByUuidApi(
      @PathVariable String uuid,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Search search = searchService.getSearchByUuid(uuid, principal.getUser());

    return SearchGetResponse.fromSearch(search);
  }

  @GetMapping(value = "/{uuid}", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView getSearchByUuidHtml(
      @PathVariable String uuid,
      Model model,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Search search = searchService.getSearchByUuid(uuid, principal.getUser());

    model.addAttribute("search", SearchGetResponse.fromSearch(search));

    return new ModelAndView("searches/search", model.asMap());
  }

  // =============================
  // DELETE SEARCH
  // =============================

  @DeleteMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<Void> deleteSearchApi(
      @PathVariable String uuid,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    searchService.deleteSearch(uuid, principal.getUser());

    return ResponseEntity.noContent().build();
  }

}
