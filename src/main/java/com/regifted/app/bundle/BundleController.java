package com.regifted.app.bundle;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.regifted.app.bundle.dto.BundleGetResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/bundles")
@RequiredArgsConstructor
public class BundleController {

  private final BundleService bundleService;

  @GetMapping(value = "/cart", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  @ResponseBody
  public BundleGetResponse getCurrentCartApi(@AuthenticationPrincipal UserDetails userDetails) {
    Bundle cart = bundleService.getCurrentCart(userDetails.getUsername());
    if (cart == null) {
      throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Cart is empty");
    }
    return BundleGetResponse.from(cart);
  }

  @GetMapping(value = "/cart", produces = MediaType.TEXT_HTML_VALUE)
  public String getCurrentCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    Bundle cart = bundleService.getCurrentCart(userDetails.getUsername());
    model.addAttribute("bundle", cart);
    return "fragments/cart :: cart-content";
  }

  @PostMapping("/cart/items/{itemUuid}")
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
