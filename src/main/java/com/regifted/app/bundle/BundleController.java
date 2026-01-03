package com.regifted.app.bundle;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;
import java.util.stream.Collectors;


import com.regifted.app.bundle.dto.BundleGetResponse;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/user/me/cart")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;
    private final UserService userService;

    // 1. Affichage HTML du panier complet
    @GetMapping("")
    public String getCurrentCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Set<Bundle> bundles = bundleService.getCurrentCart(userDetails.getUsername());
        model.addAttribute("bundles", bundles);
        return "fragments/cart :: cart-content"; 
    }


    @PostMapping(value = "/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
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


    @PatchMapping(value = "/bundle/{bundleItem}", produces = MediaType.TEXT_HTML_VALUE)
    public String u(
            @PathVariable String itemUuid,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        bundleService.removeItemFromUserBundle(itemUuid, userDetails.getUsername());
        
        Set<Bundle> bundles = bundleService.getCurrentCart(userDetails.getUsername());
        model.addAttribute("bundles", bundles);
        
        return "fragments/cart :: cart-content";
    }


    @DeleteMapping(value = "/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
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

    @PatchMapping("/{bundleUuid}/status/{newStatus}")
    public ModelAndView updateStatusAndReturnProfile(
            @PathVariable String bundleUuid,
            @PathVariable BundleStatus newStatus,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Model model) {

        if (principal == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");

        bundleService.changeStatus(bundleUuid, newStatus, principal.getUsername());

        User user = userService.getByEmail(principal.getUsername());
        model.addAttribute("user", user);

        return new ModelAndView("users/me");
    }
}