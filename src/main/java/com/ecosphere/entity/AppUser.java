package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

/** One account table for USER / NGO / NURSERY / ADMIN (tickets 1, 3, 4, 8, 7, 9, 13, 14). */
@Entity @Table(name="users") @Data
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String name; @Column(unique=true) private String email;
  private String phone, location;
  @com.fasterxml.jackson.annotation.JsonIgnore private String passwordHash;
  private String role;   // USER, NGO, NURSERY, ADMIN
  private String status; // ACTIVE, PENDING, SUSPENDED
  private int ecoPoints, greenScore;
  // profile fields
  @Column(length=1000) private String bio;
  private String profileImageUrl, organizationName, registrationNo, address, website, adminDepartment;
  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime lastLoginAt;
}
