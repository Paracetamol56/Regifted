package com.regifted.app.item;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.user.User;

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
@Table(name = "item")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Item {

  @Id
  @Column(nullable = false, unique = true)
  @EqualsAndHashCode.Include
  private String uuid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user", nullable = false)
  @JsonBackReference
  private User user;

  @Column(nullable = false)
  private String title;

  @Column(updatable = true)
  private String description;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Column(nullable = true)
  private Instant donateAt;

  @Column(nullable = false)
  private Float latitude;

  @Column(nullable = false)
  private Float longitude;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EState state;

  @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JsonManagedReference
  private Set<Keyword> keywords;

  @ManyToMany(mappedBy = "favoriteItems", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<User> likedByUsers;

  @Transient
  @JsonProperty("likesCount")
  public int getLikesCount() {
    return likedByUsers != null ? likedByUsers.size() : 0;
  }

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
