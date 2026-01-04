package com.regifted.app.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;

import com.regifted.app.cart.dto.CartGetResponse;
import com.regifted.app.cart.dto.CartItemPostRequest;
import com.regifted.app.cart.dto.CartPatchRequest;
import com.regifted.app.security.CustomUserPrincipal;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/users/me/carts")
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

  @GetMapping(value = "/{uuid}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<CartGetResponse> getSpecificCart(
      @PathVariable String uuid,
      @AuthenticationPrincipal UserDetails userDetails) {
    Cart cart = cartService.getCartByUuidForUser(uuid, userDetails.getUsername());
    CartGetResponse response = CartGetResponse.from(cart);

    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/{uuid}", produces = MediaType.TEXT_HTML_VALUE)
  public String getSpecificCartHtml(
      @PathVariable String uuid,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    Cart cart = cartService.getCartByUuidForUser(uuid, userDetails.getUsername());
    model.addAttribute("cart", cart);
    return "fragments/single-cart :: single-cart-content";
  }

  @PostMapping(value = "/items", consumes = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE }, produces = {
          MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<CartGetResponse> addItemToCartApi(
      @RequestBody CartItemPostRequest req,
      @AuthenticationPrincipal UserDetails userDetails) {
    Cart cart = cartService.addItemToUserCarts(req.getItem(), userDetails.getUsername());
    CartGetResponse response = CartGetResponse.from(cart);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/items", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
  public String addItemToCartHtml(
      @Validated @ModelAttribute CartItemPostRequest req,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {
    cartService.addItemToUserCarts(req.getItem(), userDetails.getUsername());

    Set<Cart> carts = cartService.getAllCartsForUser(userDetails.getUsername());
    model.addAttribute("bundles", carts);

    return "fragments/cart :: cart-content";
  }

  @DeleteMapping(value = "/items/{itemUuid}", produces = MediaType.TEXT_HTML_VALUE)
  public String removeItemFromCart(
      @PathVariable String itemUuid,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {

    cartService.removeItemFromUserCart(itemUuid, userDetails.getUsername());

    Set<Cart> carts = cartService.getAllCartsForUser(userDetails.getUsername());
    model.addAttribute("bundles", carts);

    return "fragments/cart :: cart-content";
  }

  @PatchMapping(value = "/{uuid}", consumes = { MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE }, produces = {
          MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
  public ResponseEntity<CartGetResponse> updateStatusApi(
      @PathVariable String uuid,
      @Validated @RequestBody CartPatchRequest req,
      @AuthenticationPrincipal UserDetails userDetails) {
    Cart cart = cartService.changeStatus(uuid, req.getStatus(), userDetails.getUsername());
    CartGetResponse response = CartGetResponse.from(cart);

    return ResponseEntity.ok(response);
  }

  @PatchMapping(value = "/{uuid}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView updateStatusAndReturnProfile(
      @PathVariable String uuid,
      @Validated @ModelAttribute CartPatchRequest req,
      @AuthenticationPrincipal CustomUserPrincipal principal,
      Model model) {
    cartService.changeStatus(uuid, req.getStatus(), principal.getUsername());

    User user = userService.getByEmail(principal.getUsername());
    model.addAttribute("user", user);

    return new ModelAndView("users/me");
  }
}
