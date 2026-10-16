package com.ecosphere.service;
import com.ecosphere.entity.SystemConfig; import com.ecosphere.repository.ConfigRepo; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.util.List;

/** Reads / writes admin editable settings (tickets 25, 34). */
@Service @RequiredArgsConstructor
public class ConfigService {
  private final ConfigRepo repo;
  public String get(String key,String def){ return repo.findById(key).map(SystemConfig::getValue).orElse(def); }
  public int getInt(String key,int def){ try{ return Integer.parseInt(get(key,String.valueOf(def)).trim()); }catch(NumberFormatException e){ return def; } }
  public double getDouble(String key,double def){ try{ return Double.parseDouble(get(key,String.valueOf(def)).trim()); }catch(NumberFormatException e){ return def; } }
  public List<SystemConfig> all(){ return repo.findAll(); }
  public SystemConfig save(String key,String value){
    if(key==null||key.isBlank()) throw new IllegalArgumentException("Config key is required.");
    if(value==null) throw new IllegalArgumentException("Config value is required.");
    return repo.save(new SystemConfig(key.trim().toUpperCase(),value.trim()));
  }
  public void delete(String key){ repo.deleteById(key); }
}
