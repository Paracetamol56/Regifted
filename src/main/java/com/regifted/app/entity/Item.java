package com.regifted.app.entity;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item")


public class Item {

  @Id
  @Column(nullable = false, unique = true)
  private String id;

  // Clef etrangère vers User one to many


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

  // Clef etrangère vers State one to many

  // Favorie : clef étrangère many to many vers user
}
