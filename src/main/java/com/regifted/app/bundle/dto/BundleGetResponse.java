package com.regifted.app.bundle.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.regifted.app.bundle.Bundle;
import com.regifted.app.item.dto.ItemGetResponse;
import com.regifted.app.user.dto.UserSummaryGetResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleGetResponse {
  private String uuid;
  private UserSummaryGetResponse receiver;
  private UserSummaryGetResponse donor;
  private Set<ItemGetResponse> items;
  private Instant createdAt;
  private Instant checkoutAt;

  public static BundleGetResponse from(Bundle bundle) {
    BundleGetResponse response = new BundleGetResponse();
    response.setUuid(bundle.getUuid());
    response.setReceiver(UserSummaryGetResponse.from(bundle.getReceiver()));
    response.setDonor(UserSummaryGetResponse.from(bundle.getDonor()));
    response.setCreatedAt(bundle.getCreatedAt());
    response.setCheckoutAt(bundle.getCheckoutAt());

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
