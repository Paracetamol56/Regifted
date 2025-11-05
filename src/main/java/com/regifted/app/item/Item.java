package com.regifted.app.item;

import java.time.Instant;
import java.util.Set;

import com.regifted.app.user.User;
import com.regifted.app.state.State;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item")

public class Item {

  @Id
  @Column(nullable = false, unique = true)
  private String id;

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

  @Column(nullable = false)
  private Instant donateAt;

  @Column(nullable = false)
  private Integer latitude;

  @Column(nullable = false)
  private Integer longitude;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "state_id", nullable = false)
  private State state;

  @ManyToMany()
  private Set<User> favoriteItems;
}
