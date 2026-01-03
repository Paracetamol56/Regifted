package com.regifted.app.cart;

import com.regifted.app.item.Item;

import com.regifted.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart")
public class Cart {

  @Id
  private String uuid = UUID.randomUUID().toString();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_uuid", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_uuid", nullable = false)
  private User owner;

  @ManyToMany(mappedBy = "carts", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  private Set<Item> items = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private CartStatus status;

  @CreationTimestamp
  private Instant createdAt;
}
