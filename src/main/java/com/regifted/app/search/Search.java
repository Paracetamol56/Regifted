package com.regifted.app.search;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.regifted.app.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "search")
public class Search {
  @Id
  @Column(nullable = false, unique = true)
  @EqualsAndHashCode.Include
  private String uuid;

  @Column(nullable = false, updatable = false)
  private User user;

  @Column(nullable = false, updatable = false)
  private String query;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
  }
}
