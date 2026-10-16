package com.ecosphere.entity;
import jakarta.persistence.*; import lombok.Data;

@Entity @Data
public class SystemConfig {
  @Id @Column(name="cfg_key") private String key;
  @Column(name="cfg_value") private String value;
  public SystemConfig(){} public SystemConfig(String k,String v){key=k;value=v;}
}
