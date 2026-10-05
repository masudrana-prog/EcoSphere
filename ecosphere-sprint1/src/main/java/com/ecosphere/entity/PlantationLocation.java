package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

/** A named planting site a user manages (ticket 6). */
@Entity @Data
public class PlantationLocation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser owner;
  private String name, address, district;
  private Double latitude, longitude;
  private boolean active = true;
  private LocalDateTime createdAt = LocalDateTime.now();
}
