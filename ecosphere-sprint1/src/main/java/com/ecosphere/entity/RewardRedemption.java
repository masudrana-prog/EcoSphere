package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

@Entity @Data
public class RewardRedemption {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser user; @ManyToOne private Reward reward;
  private int pointsSpent;
  private LocalDateTime redeemedAt = LocalDateTime.now();
}
