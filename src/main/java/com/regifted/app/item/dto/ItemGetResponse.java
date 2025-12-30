package com.regifted.app.item.dto;

import com.regifted.app.item.EState;
import com.regifted.app.item.Item;
import com.regifted.app.keyword.dto.KeywordGetResponse;
import com.regifted.app.user.dto.UserSummaryGetResponse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class ItemGetResponse {

  private String uuid;
  private String href;
  private UserSummaryGetResponse user;
  private String title;
  private String description;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant donateAt;
  private Float latitude;
  private Float longitude;
  private EState state;
  private Set<KeywordGetResponse> keywords;
  private int likes;

  public static ItemGetResponse from(Item item) {
    ItemGetResponse response = new ItemGetResponse();
    response.setUuid(item.getUuid());
    response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/items/{uuid}")
        .buildAndExpand(item.getUuid())
        .toUriString());

    // User information
    if (item.getUser() != null) {
      response.setUser(UserSummaryGetResponse.from(item.getUser()));
    }

    response.setTitle(item.getTitle());
    response.setDescription(item.getDescription());
    response.setCreatedAt(item.getCreatedAt());
    response.setUpdatedAt(item.getUpdatedAt());
    response.setDonateAt(item.getDonateAt());
    response.setLatitude(item.getLatitude());
    response.setLongitude(item.getLongitude());
    response.setState(item.getState());

    // Convert keywords to KeywordResponse DTOs
    if (item.getKeywords() != null) {
      response.setKeywords(item.getKeywords().stream()
          .map(KeywordGetResponse::from)
          .collect(Collectors.toSet()));
    }

    response.setLikes(item.getLikes());

    return response;
  }
}
