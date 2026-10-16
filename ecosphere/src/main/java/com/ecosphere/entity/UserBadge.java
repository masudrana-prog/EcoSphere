package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

@Entity @Data @Table(uniqueConstraints=@UniqueConstraint(columnNames={"user_id","badge_id"}))
public class UserBadge {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser user; @ManyToOne private Badge badge;
  private LocalDateTime awardedAt = LocalDateTime.now();
}
