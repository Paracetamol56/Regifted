package com.regifted.app.bundle;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.regifted.app.item.Item;
import com.regifted.app.user.User;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.regifted.app.bundle.dto.BundleGetResponse;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/bundle")
@RequiredArgsConstructor
public class BundleController {

  private final BundleService bundleService;

    @GetMapping("/cart")
    public String getCurrentCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Bundle cart = bundleService.getCurrentCart(userDetails.getUsername());

        if (cart != null) {
            Map<User, List<Item>> itemsByDonor = cart.getItems().stream()
                .collect(Collectors.groupingBy(Item::getUser));
                
            model.addAttribute("itemsByDonor", itemsByDonor);
        }

        model.addAttribute("bundle", cart);
        return "fragments/cart :: cart-content"; 
    }

    @GetMapping(value = "/cart", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @ResponseBody
    public BundleGetResponse getCurrentCartApi(@AuthenticationPrincipal UserDetails userDetails) {
        Bundle cart = bundleService.getCurrentCart(userDetails.getUsername());
        if (cart == null) {
        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Cart is empty");
        }
        return BundleGetResponse.from(cart);
    }

    @PostMapping("/cart/items/{uuid}") // Vérifiez bien ce mapping
    public String addItemToCart(@PathVariable String uuid, Principal principal, Model model) {
        Bundle cart = bundleService.addItemToCart(uuid, principal.getName());

        Map<User, List<Item>> itemsByDonor = cart.getItems().stream()
                .collect(Collectors.groupingBy(Item::getUser));

        model.addAttribute("bundle", cart);
        model.addAttribute("itemsByDonor", itemsByDonor);

        return "fragments/cart :: cart-content";
    }

    @PostMapping(value = "/cart/items/{itemUuid}", produces = { MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<?> addItemToCartApi(
        @PathVariable String itemUuid,
        @AuthenticationPrincipal UserDetails userDetails) {
        try {
        Bundle updatedBundle = bundleService.addItemToCart(itemUuid, userDetails.getUsername());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/bundles/cart")
            .buildAndExpand(updatedBundle.getUuid())
            .toUri();
        return ResponseEntity.created(location).build();
        } catch (IllegalStateException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping(value = "/cart/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
    public String addItemToCart(
        @PathVariable String itemUuid,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
        try {
        Bundle updatedBundle = bundleService.addItemToCart(itemUuid, userDetails.getUsername());
        model.addAttribute("bundle", updatedBundle);
        return "fragments/cart :: cart-content";
        } catch (IllegalStateException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(value = "/cart/items/{itemUuid}", produces = { MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<?> removeItemFromCartApi(
        @PathVariable String itemUuid,
        @AuthenticationPrincipal UserDetails userDetails) {
        Bundle updatedBundle = bundleService.removeItemFromCart(itemUuid, userDetails.getUsername());
        if (updatedBundle == null) {
        return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(BundleGetResponse.from(updatedBundle));
    }

    @DeleteMapping("/cart/items/{itemUuid}")
    public String removeItemFromCart(
        @PathVariable String itemUuid,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
        Bundle updatedBundle = bundleService.removeItemFromCart(itemUuid, userDetails.getUsername());
        model.addAttribute("bundle", updatedBundle);
        return "fragments/cart :: cart-content";
    }
}

