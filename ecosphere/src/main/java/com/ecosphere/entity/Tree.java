package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data;

/** Nursery stock line (tickets 20, 21). */
@Entity @Data
public class Tree {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser nursery;
  @ManyToOne private TreeSpecies species;
  private String name, scientificName, category; private double price; private int quantity;
  private int lowStockThreshold = 100;
  private boolean listed = true; // nursery can hide a line without deleting it
  public boolean isAvailable(){ return listed && quantity>0; }
  public boolean isLowStock(){ return quantity>0 && quantity<lowStockThreshold; }
}
