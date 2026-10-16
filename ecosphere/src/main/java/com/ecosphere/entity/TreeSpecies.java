package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data;

/** Reference information about a species, managed by admin (tickets 17, 29). */
@Entity @Data
public class TreeSpecies {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(unique=true) private String name;
  private String scientificName, category, growthRate, imageUrl;
  @Column(length=2000) private String description, careInstructions, habitat;
  private double co2KgPerYear = 22.5;
  private int matureHeightMeters, maturityYears;
  private boolean active = true;
}
