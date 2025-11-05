package com.regifted.app.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "search")

/**
 * Je pense qu'il faut permettre uniquement de créer et de supprimer une
 * recherche et non de la
 * modifiée
 */
public class Search {
  @Id
  @Column(nullable = false, updatable = false)
  private String userid;

  // Clef etrangère vers State ManyToMany

  // Clef étangère vers Keywords ManyToMany

  @Column(nullable = false, updatable = false)
  private Integer latitude;

  @Column(nullable = false, updatable = false)
  private Integer longitude;
}
