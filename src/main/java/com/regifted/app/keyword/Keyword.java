package com.regifted.app.keyword;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.regifted.app.item.Item;

@Data
@Entity
@Table(name = "keyword")

public class Keyword {
  @Id
  private String uuid;

  @Column(nullable = false, unique = true)
  private String name;

  @PrePersist
  public void generateId() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
  }
}
