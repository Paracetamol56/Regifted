package com.regifted.app.bundle;

import com.regifted.app.user.User;
import com.regifted.app.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.regifted.app.item.ItemRepository;
import com.regifted.app.item.Item;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BundleService {

    private final BundleRepository bundleRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    public Bundle getCurrentCart(String receiverEmail) {
        User receiver = userService.getByEmail(receiverEmail);
        return bundleRepository.findByReceiverAndCheckoutAtIsNull(receiver).orElse(null);
    }

    @Transactional
    public Bundle addItemToCart(String itemUuid, String receiverEmail) {
        User receiver = userService.getByEmail(receiverEmail);
        Item item = itemRepository.findById(itemUuid)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getUser().equals(receiver)) {
            throw new IllegalStateException("You cannot add your own item to your cart");
        }

        Bundle cart = bundleRepository.findByReceiverAndCheckoutAtIsNull(receiver).orElse(null);

        if (cart == null) {
            cart = new Bundle();
            cart.setReceiver(receiver);
            cart.setDonor(item.getUser());
            cart = bundleRepository.save(cart);
        } else {
            if (cart.getDonor() != null && !cart.getDonor().equals(item.getUser())) {
                throw new IllegalStateException("A bundle can only contain items from the same donor. Clear your cart first.");
            }
        }

        item.setBundle(cart);
        cart.getItems().add(item);
        
        itemRepository.save(item);
        return bundleRepository.save(cart);
    }

    @Transactional
    public Bundle removeItemFromCart(String itemUuid, String receiverEmail) {
        Bundle cart = getCurrentCart(receiverEmail);
        if (cart == null) return null;

        Item item = itemRepository.findById(itemUuid)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setBundle(null);
        cart.getItems().remove(item);
        itemRepository.save(item);

        if (cart.getItems().isEmpty()) {
            bundleRepository.delete(cart);
            return null;
        }

        return bundleRepository.save(cart);
    }

    @Transactional
    public void clearCart(String receiverEmail) {
        Bundle cart = getCurrentCart(receiverEmail);
        if (cart != null) {
            for (Item item : cart.getItems()) {
                item.setBundle(null);
                itemRepository.save(item);
            }
            bundleRepository.delete(cart);
        }
    }
}