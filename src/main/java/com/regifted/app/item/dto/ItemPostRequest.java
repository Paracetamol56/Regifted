package com.regifted.app.item.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.regifted.app.state.State;
import com.regifted.app.item.Item;
import com.regifted.app.user.User;

import lombok.Data;

@Data
public class ItemPostRequest {
  private String title;
  private String description;
  private Integer latitude;
  private Integer longitude;
  private Integer state;

  public Item toItem() {
    Item item = new Item();

    // item.setUser(UUID.randomUUID().toString());
    item.setTitle(this.title);
    item.setDescription(this.description);
    item.setLatitude(this.latitude);
    item.setLongitude(this.longitude);
    item.setState(0);

    Instant now = Instant.now();
    item.setPublishAt(now);
    item.setUpdateAt(now);

    item.setFavoriteItems(Set.of()); // default empty set

    return item;
  }
}
