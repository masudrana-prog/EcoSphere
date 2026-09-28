package com.ecosphere;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;
@Entity @Table(name="users") @Data
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String name; @Column(unique=true) private String email;
  private String phone, location, passwordHash;
  private String role;   // USER, NGO, NURSERY, ADMIN
  private String status; // ACTIVE, PENDING, SUSPENDED
  private int ecoPoints, greenScore;
  private LocalDateTime createdAt = LocalDateTime.now();
}
