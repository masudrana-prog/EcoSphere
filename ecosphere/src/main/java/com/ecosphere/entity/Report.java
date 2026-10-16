package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

/** Complaint / report raised by any user (tickets 24, 32). */
@Entity @Data
public class Report {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser reporter; @ManyToOne private AppUser against;
  private String type = "COMPLAINT"; // COMPLAINT, FAKE_PLANTATION, ORDER_ISSUE, ABUSE, BUG
  private String subject; @Column(length=2000) private String description;
  private String status = "OPEN";
  @Column(length=1000) private String adminResponse;
  @ManyToOne private AppUser handledBy;
  private LocalDateTime createdAt = LocalDateTime.now(), resolvedAt;
}
