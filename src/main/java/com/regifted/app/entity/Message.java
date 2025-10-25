package com.regifted.app.entity;


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
