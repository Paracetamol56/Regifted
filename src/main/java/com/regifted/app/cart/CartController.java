package com.regifted.app.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;

import com.regifted.app.cart.dto.CartGetResponse;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/users/me/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;
  private final UserService userService;

  @GetMapping(value = "", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<Page<CartGetResponse>> getCurrentCart(
      @AuthenticationPrincipal UserDetails userDetails,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<Cart> carts = cartService.getCartsForUser(userDetails.getUsername(), pageable);
    Page<CartGetResponse> response = carts.map(CartGetResponse::from);

    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "", produces = MediaType.TEXT_HTML_VALUE)
  public String getCurrentCartHtml(@AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    Set<Cart> carts = cartService.getAllCartsForUser(userDetails.getUsername());
    model.addAttribute("carts", carts);
    return "fragments/cart :: cart-content";
  }

  @PostMapping(value = "/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
  public String addItemToCart(
      @PathVariable String itemUuid,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    try {
      cartService.addItemToUserBundles(itemUuid, userDetails.getUsername());

      // On recharge la liste complète pour que le fragment affiche tous les lots
      Set<Cart> carts = cartService.getAllCartsForUser(userDetails.getUsername());
      model.addAttribute("bundles", carts);

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

    cartService.removeItemFromUserBundle(itemUuid, userDetails.getUsername());

    Set<Cart> carts = cartService.getAllCartsForUser(userDetails.getUsername());
    model.addAttribute("bundles", carts);

    return "fragments/cart :: cart-content";
  }

  @DeleteMapping(value = "/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
  public String removeItemFromCart(
      @PathVariable String itemUuid,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {

    cartService.removeItemFromUserBundle(itemUuid, userDetails.getUsername());

    Set<Cart> carts = cartService.getAllCartsForUser(userDetails.getUsername());
    model.addAttribute("bundles", carts);

    return "fragments/cart :: cart-content";
  }

  @PatchMapping("/validate/{bundleUuid}")
  public String validateSpecificBundle(
      @PathVariable String bundleUuid,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {

    cartService.validateBundle(bundleUuid, userDetails.getUsername());

    Set<Cart> carts = cartService.getAllCartsForUser(userDetails.getUsername());
    model.addAttribute("bundles", carts);

    return "fragments/cart :: cart-content";
  }

  @PatchMapping("/{bundleUuid}/status/{newStatus}")
  public ModelAndView updateStatusAndReturnProfile(
      @PathVariable String bundleUuid,
      @PathVariable CartStatus newStatus,
      @AuthenticationPrincipal CustomUserPrincipal principal,
      Model model) {

    if (principal == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");

    cartService.changeStatus(bundleUuid, newStatus, principal.getUsername());

    User user = userService.getByEmail(principal.getUsername());
    model.addAttribute("user", user);

    return new ModelAndView("users/me");
  }
}
