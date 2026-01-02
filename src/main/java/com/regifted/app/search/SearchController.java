package com.regifted.app.search;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.search.dto.SearchGetResponse;
import com.regifted.app.search.dto.SearchPostRequest;
import com.regifted.app.security.CustomUserPrincipal;

import org.springframework.web.bind.annotation.ModelAttribute;

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

  @PostMapping(value = "", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ResponseBody // On renvoie du HTML brut pour HTMX
  public String createSearchHtmx(
      @ModelAttribute SearchPostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal) {
    try {
      searchService.createSearch(req.getQuery(), principal.getUser());
      return "<button type='button' class='secondary' disabled style='width: 100%;'>" +
          "Saved !" +
          "</button>";

    } catch (Exception e) {
      return "<button type='submit' class='secondary'>Erreur (Réessayer)</button>";
    }
  }

  @PostMapping(value = "", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<SearchGetResponse> createSearchApi(
      @Valid @RequestBody SearchPostRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    if (principal == null)
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    Search created = searchService.createSearch(req.getQuery(), principal.getUser());
    URI location = URI.create("/searches/" + created.getUuid());

    return ResponseEntity.created(location).body(SearchGetResponse.from(created));
  }

  // =============================
  // GET SEARCHES
  // =============================

  @GetMapping(value = "", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public Page<SearchGetResponse> getSearchesApi(
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @AuthenticationPrincipal CustomUserPrincipal principal) {

    Page<Search> searches = searchService.getAllSearchesForUser(principal.getUser(), pageable);

    return searches.map(SearchGetResponse::from);
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

    return SearchGetResponse.from(search);
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
