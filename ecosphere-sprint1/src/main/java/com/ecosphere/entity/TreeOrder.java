package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;

@Entity @Table(name="tree_orders") @Data
public class TreeOrder {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne private AppUser buyer; @ManyToOne private AppUser nursery; @ManyToOne private Tree tree;
  private int quantity; private double unitPrice, total;
  private String deliveryAddress, phone, paymentMethod; // COD or BKASH
  private String bkashSender; @Column(unique=true) private String trxId;
  private String status = "PENDING"; // PENDING, CONFIRMED, DELIVERED, CANCELLED
  private LocalDateTime createdAt = LocalDateTime.now();
}
