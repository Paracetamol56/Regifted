package com.regifted.app.search.dto;

import java.time.Instant;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.search.Search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchGetResponse {
  private String uuid;
  private String href;
  private String query;
  private Instant createdAt;

  public static SearchGetResponse from(Search search) {
    return new SearchGetResponse(
        search.getUuid(),
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/users/me/searches/{uuid}")
            .buildAndExpand(search.getUuid())
            .toUriString(),
        search.getQuery(),
        search.getCreatedAt());
  }
}
