package com.regifted.app.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchPostRequest {
  @NotBlank(message = "Query cannot be empty")
  @Size(max = 500, message = "Query cannot exceed 500 characters")
  private String query;


  public SearchPostRequest(){}
}
