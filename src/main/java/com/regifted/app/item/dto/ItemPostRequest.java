package com.regifted.app.item.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.regifted.app.item.Item;
import com.regifted.app.keyword.Keyword;

import lombok.Data;

@Data
public final class ItemPostRequest extends ItemBaseRequest {

    public Item toItem(Set<Keyword> keywordEntities) {
        Item item = new Item();

        item.setTitle(this.title);
        item.setDescription(this.description);
        item.setLatitude(this.latitude);
        item.setLongitude(this.longitude);
        item.setState(this.state);

        Instant now = Instant.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        item.setFavoriteItems(Set.of()); // par défaut vide
        item.setKeyword(keywordEntities != null ? keywordEntities : new HashSet<>());

        return item;
    }
}
