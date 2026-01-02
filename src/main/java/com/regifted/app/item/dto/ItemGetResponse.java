package com.regifted.app.item.dto;

import com.regifted.app.bundle.Bundle;
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
    private String bundleUuid; 
    private boolean inCurrentCart;

    public static ItemGetResponse from(Item item) {
        return from(item, null);
    }

    public static ItemGetResponse from(Item item, Bundle currentCart) {
        ItemGetResponse response = new ItemGetResponse();
        response.setUuid(item.getUuid());
        response.setHref(ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/items/{uuid}")
            .buildAndExpand(item.getUuid())
            .toUriString());

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

        if (item.getKeywords() != null) {
            response.setKeywords(item.getKeywords().stream()
                .map(KeywordGetResponse::from)
                .collect(Collectors.toSet()));
        }

        response.setLikes(item.getLikes());

        if (item.getBundle() != null) {
            String bUuid = item.getBundle().getUuid();
            response.setBundleUuid(bUuid);
            
            // Si un panier est fourni, on vérifie si cet item en fait partie
            if (currentCart != null && bUuid.equals(currentCart.getUuid())) {
                response.setInCurrentCart(true);
            }
        }

        return response;
    }
}