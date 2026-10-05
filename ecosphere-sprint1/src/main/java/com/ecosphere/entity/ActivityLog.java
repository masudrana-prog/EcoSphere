package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

/** Audit trail used by activity monitoring and system monitoring (tickets 24, 30, 34). */
@Entity @Data
public class ActivityLog {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String actorEmail, actorRole, action, entityType; private Long entityId;
  @Column(length=500) private String details;
  private LocalDateTime occurredAt = LocalDateTime.now();
}
