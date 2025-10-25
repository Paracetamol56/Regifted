package com.regifted.app.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "message")


public class Message {

  // Sender : Clef étrangère OneToMany vers User
  // receiver : Clef étrangère OnToMany vers User

  @Column(nullable = false, updatable = true)
  private String content;

  @Column(nullable = false)
  private Instant sendAt;

}
