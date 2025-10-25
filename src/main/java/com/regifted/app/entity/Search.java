package com.regifted.app.entity;


@Data
@Entity
@Table(name = "search")



/**
 * Je pense qu'il faut permettre uniquement de créer
 * et de supprimer une recherche et non de la modifiée
 */
public class Search {
  @Id
  @column(nullable = false, updatable = false)
  private String userid;

  // Clef etrangère vers State ManyToMany
  
  // Clef étangère vers Keywords ManyToMany

  @column(nullable = false, updatable = false)
  private Integre latitude;

  @column(nullable = false, updatable = false)
  private Integer longitude;
}
