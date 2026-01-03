package com.regifted.app.bundle;

import com.regifted.app.user.User;
import com.regifted.app.user.UserService;
import lombok.RequiredArgsConstructor;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.regifted.app.item.ItemRepository;
import com.regifted.app.item.Item;

@Service
@RequiredArgsConstructor
public class BundleService {

  private final BundleRepository bundleRepository;
  private final ItemRepository itemRepository;
  private final UserService userService;

  public Set<Bundle> getCurrentCart(String email) {
      User user = userService.getByEmail(email);
      Set<Bundle> bundles = user.getBundles();
      return bundles;
  }

  @Transactional
    public Bundle addItemToUserBundles(String itemUuid, String email) {
        User receiver = userService.getByEmail(email);
        Item item = itemRepository.findById(itemUuid)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getUser().equals(receiver)) {
            throw new IllegalStateException("You cannot add your own item to your cart");
        }

        Bundle currentBundle = receiver.getBundles().stream()
                .filter(b -> b.getDonor().getUuid().equals(item.getUser().getUuid()))
                .filter(b -> b.getStatus() == BundleStatus.DRAFT)
                .findFirst()
                .orElseGet(() -> {
                    Bundle newBundle = new Bundle();
                    newBundle.setUser(receiver);
                    newBundle.setDonor(item.getUser());
                    newBundle.setStatus(BundleStatus.DRAFT);
                    Bundle saved = bundleRepository.save(newBundle);
                    receiver.getBundles().add(saved); 
                    return saved;
                });

        if (!currentBundle.getItems().contains(item)) {
            currentBundle.getItems().add(item);
            item.getBundles().add(currentBundle);
        }

        itemRepository.save(item);
        return bundleRepository.save(currentBundle);
    }

    @Transactional
    public Bundle removeItemFromUserBundle(String itemUuid, String email) {
        Item item = itemRepository.findById(itemUuid)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Bundle cart = item.getBundles().stream()
                .filter(b -> b.getUser().getEmail().equals(email))
                .filter(b -> b.getStatus() == BundleStatus.DRAFT)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item is not in your cart"));

        cart.getItems().remove(item);
        item.getBundles().remove(cart);
        itemRepository.save(item);

        if (cart.getItems().isEmpty()) {
            cart.getUser().getBundles().remove(cart);
            bundleRepository.delete(cart);
            return null;
        }

        return bundleRepository.save(cart);
    }

    @Transactional
    public void validateBundle(String bundleUuid, String email) {
        Bundle bundle = bundleRepository.findById(bundleUuid)
                .orElseThrow(() -> new RuntimeException("Lot non trouvé"));

        if (!bundle.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("Ce lot ne vous appartient pas");
        }

        if (bundle.getItems().isEmpty()) {
            throw new IllegalStateException("Impossible de valider un lot vide");
        }

        bundle.setStatus(BundleStatus.SENT);
        bundleRepository.save(bundle);
    }

    @Transactional
    public void changeStatus(String bundleUuid, BundleStatus newStatus, String email) {
        Bundle bundle = bundleRepository.findById(bundleUuid)
                .orElseThrow(() -> new RuntimeException("Lot introuvable"));

        if (newStatus == BundleStatus.SENT) {
            if (!bundle.getUser().getEmail().equals(email)) throw new AccessDeniedException("Action interdite");
        } else if (newStatus == BundleStatus.ACCEPTED || newStatus == BundleStatus.REFUSED) {
            if (!bundle.getDonor().getEmail().equals(email)) throw new AccessDeniedException("Action interdite");
        }

        bundle.setStatus(newStatus);
        bundleRepository.save(bundle);
    }   
}
