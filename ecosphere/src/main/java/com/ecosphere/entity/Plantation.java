package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.*;

@Entity @Data
public class Plantation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser user; @ManyToOne private AppUser verifiedBy;
  @ManyToOne private PlantationLocation savedLocation;
  private String treeName, location, photoUrl, rejectReason;
  private String status = "PENDING"; // PENDING, VERIFIED, REJECTED
  private Double lat, lng; private LocalDate plantedOn; private int pointsAwarded;
  private LocalDateTime submittedAt = LocalDateTime.now(), verifiedAt;
}
