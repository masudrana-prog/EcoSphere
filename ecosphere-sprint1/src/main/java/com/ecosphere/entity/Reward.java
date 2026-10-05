package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data;

/** EcoReward users can redeem with EcoPoints (ticket 31). */
@Entity @Data
public class Reward {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String name; @Column(length=1000) private String description;
  private int pointsCost, stock;
  private boolean active = true;
}
