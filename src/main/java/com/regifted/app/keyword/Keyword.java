package com.regifted.app.keyword;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

import com.regifted.app.item.Item;

@Data
@Entity
@Table(name = "keyword")

public class Keyword {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @ManyToMany(mappedBy = "keyword")
  private Set<Item> items = new HashSet<>();
}
