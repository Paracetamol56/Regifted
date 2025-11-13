package com.regifted.app.item.dto;

import java.time.Instant;

import com.regifted.app.item.Item;

public final class ItemPostRequest extends ItemBaseRequest {

  public Item toItem() {
    Item item = new Item();

    item.setTitle(this.title);
    item.setDescription(this.description);
    item.setLatitude(this.latitude);
    item.setLongitude(this.longitude);
    item.setState(this.state);

    Instant now = Instant.now();
    item.setCreatedAt(now);
    item.setUpdatedAt(now);

    return item;
  }
}
