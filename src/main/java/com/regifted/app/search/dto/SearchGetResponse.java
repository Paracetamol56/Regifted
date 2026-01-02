package com.regifted.app.search.dto;

import java.time.Instant;

import com.regifted.app.search.Search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchGetResponse {
  private String uuid;
  private String query;
  private Instant createdAt;

  public static SearchGetResponse from(Search search) {
    return new SearchGetResponse(
        search.getUuid(),
        search.getQuery(),
        search.getCreatedAt());
  }
}
