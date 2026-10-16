package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

/** A citizen / NGO asking a nursery for saplings (ticket 22). */
@Entity @Data
public class PlantationRequest {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser requester; @ManyToOne private AppUser nursery; @ManyToOne private Tree tree;
  private int quantity;
  @Column(length=1000) private String message, response;
  private String status = "REQUESTED"; // REQUESTED, ACCEPTED, DECLINED, FULFILLED
  private LocalDateTime createdAt = LocalDateTime.now(), respondedAt;
}
