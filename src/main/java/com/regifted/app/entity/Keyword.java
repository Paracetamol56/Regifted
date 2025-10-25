package com.regifted.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "keyword")

public class Keyword {
  @Id
  @Column(nullable = false, unique = true)
  private String id;


  // @Matéo est ce qu'on met ca à unique
  // (c'est relou à dev mais c'est mieux pour la table
  // en plus à quoi sert la relation many to many, si il n'est pas unique)
  @Column(nullable = false)
  private String keyword;

  // Clef etrangère vers Search many to many
  // Clef etrangère vers Item many to many
}
