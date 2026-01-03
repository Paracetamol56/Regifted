package com.regifted.app.bundle;

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
@Table(name = "bundle")
public class Bundle {

  @Id
  private String uuid = UUID.randomUUID().toString();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_uuid", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "donor_uuid", nullable = false)
  private User donor;

  @ManyToMany(mappedBy = "bundles", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  private Set<Item> items = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private BundleStatus status;

  @CreationTimestamp
  private Instant createdAt;
}
