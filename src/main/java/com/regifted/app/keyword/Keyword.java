package com.regifted.app.keyword;

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

  @Column(nullable = false, unique = true)
  private String keyword;
}
