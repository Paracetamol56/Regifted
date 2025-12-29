package com.regifted.app.search.dto;

import com.regifted.app.search.Search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchGetResponse {
  private String uuid;
  private String userId;
  private String query;

  public static SearchGetResponse fromSearch(Search search) {
    return new SearchGetResponse(
        search.getUuid(),
        search.getUser().getUuid(),
        search.getQuery());
  }
}
