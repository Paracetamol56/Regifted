package com.regifted.app.item;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.regifted.app.user.User;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item")
public class Item {

  @Id
  @Column(nullable = false, unique = true)
  private String uuid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false, nullable = false)
  private User user;

  @Column(updatable = true, nullable = false)
  private String title;

  @Column(updatable = true)
  private String description;

  @Column(nullable = false)
  private Instant publishAt;

  @Column(nullable = false)
  private Instant updateAt;

  @Column(nullable = true)
  private Instant donateAt;

  @Column(nullable = false)
  private Integer latitude;

  @Column(nullable = false)
  private Integer longitude;

  @Column(nullable = false)
  private Integer state;

  @ManyToMany()
  private Set<User> favoriteItems;

  @PrePersist
  public void prePersist() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
  }
}
