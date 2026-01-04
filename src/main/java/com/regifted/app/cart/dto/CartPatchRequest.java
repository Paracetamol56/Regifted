package com.regifted.app.cart.dto;

import com.regifted.app.cart.CartStatus;

import lombok.Data;

@Data
public class CartPatchRequest {
  private CartStatus status;
}
