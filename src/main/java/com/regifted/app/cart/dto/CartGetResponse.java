package com.regifted.app.cart.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.cart.Cart;
import com.regifted.app.cart.CartStatus;
import com.regifted.app.item.dto.ItemGetResponse;
import com.regifted.app.user.dto.UserSummaryGetResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartGetResponse {
  private String uuid;
  private String href;
  private UserSummaryGetResponse receiver;
  private UserSummaryGetResponse donor;
  private Set<ItemGetResponse> items;
  private CartStatus status;
  private Instant createdAt;

  public static CartGetResponse from(Cart bundle) {
    CartGetResponse response = new CartGetResponse();
    response.setUuid(bundle.getUuid());

    response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/users/me/carts/{uuid}")
        .buildAndExpand(bundle.getUuid())
        .toUriString());

    // Correction des mappings acteurs
    response.setReceiver(UserSummaryGetResponse.from(bundle.getUser()));
    response.setDonor(UserSummaryGetResponse.from(bundle.getOwner()));

    response.setStatus(bundle.getStatus());
    response.setCreatedAt(bundle.getCreatedAt());

    if (bundle.getItems() != null) {
      response.setItems(bundle.getItems().stream()
          .map(ItemGetResponse::from)
          .collect(java.util.stream.Collectors.toSet()));
    } else {
      response.setItems(new HashSet<>());
    }

    return response;
  }
}
