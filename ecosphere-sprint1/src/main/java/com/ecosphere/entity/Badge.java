package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data;

/** Badge definition, managed by admin (tickets 12, 31). */
@Entity @Data
public class Badge {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(unique=true) private String name;
  private String description, icon = "🏅";
  private String criteriaType;   // see Const.CRIT_*
  private int threshold = 1;
  private int bonusPoints;
  private boolean active = true;
}
