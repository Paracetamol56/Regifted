package com.regifted.app.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CartItemPostRequest {

  @NotBlank(message = "Item UUID is required")
  @Size(min = 36, max = 36, message = "Item UUID must be 36 characters")
  private String item;
}
