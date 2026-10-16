package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

/** In-app notification (tickets 23, 33). */
@Entity @Data
public class Notification {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser recipient;
  private String title; @Column(length=1000) private String message;
  private String type = "INFO"; // INFO, PLANTATION, ORDER, REWARD, REQUEST, REPORT, BROADCAST
  private String link;
  private boolean seen; // not "read": READ is a reserved word in MySQL
  private LocalDateTime createdAt = LocalDateTime.now();
}
