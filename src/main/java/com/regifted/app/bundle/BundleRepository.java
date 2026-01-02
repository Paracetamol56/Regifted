package com.regifted.app.bundle;

import com.regifted.app.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BundleRepository extends JpaRepository<Bundle, String> {
    Optional<Bundle> findByReceiverAndCheckoutAtIsNull(User receiver);
}