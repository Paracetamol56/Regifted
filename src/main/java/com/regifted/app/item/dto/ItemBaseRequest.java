package com.regifted.app.item.dto;

import java.util.Set;

import com.regifted.app.item.EState;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public abstract class ItemBaseRequest {

  @NotBlank(message = "Title is required")
  @Size(max = 100, message = "Title must be at most 100 characters")
  protected String title;

  @NotBlank(message = "Description is required")
  @Size(max = 500, message = "Description must be at most 500 characters")
  protected String description;

  protected Set<@NotBlank(message = "Keyword cannot be blank") String> keywords;

  @NotNull(message = "Latitude is required")
  @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
  @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
  protected Float latitude;

  @NotNull(message = "Longitude is required")
  @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
  @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
  protected Float longitude;

  @NotNull(message = "State is required")
  protected EState state;
}
