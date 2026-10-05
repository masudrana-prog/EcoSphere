package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

@Entity @Data
public class Consumption {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser user;
  private String category; private double amount, co2;
  private String note;
  private LocalDateTime loggedAt = LocalDateTime.now();
}
