package com.regifted.app.item.dto;

import java.time.Instant;
import java.util.Set;

import com.regifted.app.item.EState;
import com.regifted.app.item.Item;

import lombok.Data;

@Data
public class ItemPostRequest {
  private String title;
  private String description;
  private Float latitude;
  private Float longitude;
  private EState state;

  public Item toItem() {
    Item item = new Item();

    // item.setUser(UUID.randomUUID().toString());
    item.setTitle(this.title);
    item.setDescription(this.description);
    item.setLatitude(this.latitude);
    item.setLongitude(this.longitude);
    item.setState(this.state);

    System.out.println(this);
    System.out.println(item);

    Instant now = Instant.now();
    item.setCreatedAt(now);
    item.setUpdatedAt(now);

    item.setFavoriteItems(Set.of()); // default empty set

    return item;
  }
}
