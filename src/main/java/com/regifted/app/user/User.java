package com.regifted.app.user;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.regifted.app.item.Item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {

  @Id
  @Column(nullable = false, updatable = false)
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

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @JsonManagedReference
  private Set<Item> items;

  @ManyToMany(fetch = FetchType.LAZY)
  @JsonManagedReference
  private Set<Item> favoriteItems;

  @PrePersist
  public void prePersist() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
    this.updatedAt = Instant.now();
    if (this.createdAt == null) {
      this.createdAt = this.updatedAt;
    }
  }
}
