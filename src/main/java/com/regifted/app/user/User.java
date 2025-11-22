package com.regifted.app.user;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.regifted.app.item.Item;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

  @Id
  @Column(nullable = false, updatable = false)
  @EqualsAndHashCode.Include
  private String uuid;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  @JsonIgnore
  private String password;

  @Column(nullable = false)
  private boolean notification = true;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "user", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
  @JsonManagedReference
  private Set<Item> items;

  @ManyToMany(fetch = FetchType.LAZY)
  // @JoinTable(name = "user_favorite_items", joinColumns = @JoinColumn(name =
  // "user_uuid", referencedColumnName = "uuid"), inverseJoinColumns =
  // @JoinColumn(name = "item_uuid", referencedColumnName = "uuid"))
  @JsonManagedReference
  private Set<Item> favoriteItems;

  @PrePersist
  public void prePersist() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
  }
}
