package com.regifted.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "state")

public class State {
  @Id
  @Column(nullable = false)
  private String state_name;
}
