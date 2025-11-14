package com.regifted.app.item.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemSearchRequest {

  @Min(value = 0, message = "Page must be greater or equal to 0")
  private Integer page = 0;

  @Min(value = 1, message = "Limit must be at least 1")
  @Max(value = 100, message = "Limit cannot exceed 100")
  private Integer limit = 10;

  @Size(max = 100, message = "Search query cannot exceed 100 characters")
  private String q;

  @Size(max = 50, message = "Keyword filter cannot exceed 50 characters")
  private String keyword;
}
