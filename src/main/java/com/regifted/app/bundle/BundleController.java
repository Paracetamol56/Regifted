package com.regifted.app.bundle;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;


import com.regifted.app.bundle.dto.BundleGetResponse;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/user/me/cart")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;

    // 1. Affichage HTML du panier complet
    @GetMapping("")
    public String getCurrentCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Set<Bundle> bundles = bundleService.getCurrentCart(userDetails.getUsername());
        model.addAttribute("bundles", bundles);
        return "fragments/cart :: cart-content"; 
    }


    @PostMapping(value = "/cart/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
    public String addItemToCart(
            @PathVariable String itemUuid,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        try {
            bundleService.addItemToUserBundles(itemUuid, userDetails.getUsername());
            
            // On recharge la liste complète pour que le fragment affiche tous les lots
            Set<Bundle> bundles = bundleService.getCurrentCart(userDetails.getUsername());
            model.addAttribute("bundles", bundles);
            
            return "fragments/cart :: cart-content";
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @PatchMapping(value = "/cart/bundle/{bundleItem}", produces = MediaType.TEXT_HTML_VALUE)
    public String u(
            @PathVariable String itemUuid,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        bundleService.removeItemFromUserBundle(itemUuid, userDetails.getUsername());
        
        Set<Bundle> bundles = bundleService.getCurrentCart(userDetails.getUsername());
        model.addAttribute("bundles", bundles);
        
        return "fragments/cart :: cart-content";
    }


    @DeleteMapping(value = "/cart/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
    public String removeItemFromCart(
            @PathVariable String itemUuid,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        bundleService.removeItemFromUserBundle(itemUuid, userDetails.getUsername());
        
        Set<Bundle> bundles = bundleService.getCurrentCart(userDetails.getUsername());
        model.addAttribute("bundles", bundles);
        
        return "fragments/cart :: cart-content";
    }

    @PatchMapping("/validate/{bundleUuid}")
    public String validateSpecificBundle(
            @PathVariable String bundleUuid,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        bundleService.validateBundle(bundleUuid, userDetails.getUsername());
        
        Set<Bundle> bundles = bundleService.getCurrentCart(userDetails.getUsername());
        model.addAttribute("bundles", bundles);
        
        return "fragments/cart :: cart-content";
    }

   
}