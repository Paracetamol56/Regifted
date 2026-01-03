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

import org.springframework.http.HttpStatus;

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

    @PostMapping("/cart/items/{uuid}") // Vérifiez bien ce mapping
    public String addItemToCart(@PathVariable String uuid, Principal principal, Model model) {
        Bundle cart = bundleService.addItemToCart(uuid, principal.getName());

        Map<User, List<Item>> itemsByDonor = cart.getItems().stream()
                .collect(Collectors.groupingBy(Item::getUser));

        model.addAttribute("bundle", cart);
        model.addAttribute("itemsByDonor", itemsByDonor);

        return "fragments/cart :: cart-content";
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