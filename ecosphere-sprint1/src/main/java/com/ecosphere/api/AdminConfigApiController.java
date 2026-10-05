package com.ecosphere.api;
import com.ecosphere.entity.SystemConfig; import com.ecosphere.service.ConfigService; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.util.*;

/** Admin editable system settings (e.g. ECOPOINTS_PER_TREE). */
@RestController @RequestMapping("/api/admin/config") @RequiredArgsConstructor
public class AdminConfigApiController {
  private final ConfigService config;
  public record ConfigBody(String key,String value) {}
  @GetMapping List<SystemConfig> all(){ return config.all(); }
  @PutMapping SystemConfig save(@RequestBody ConfigBody b){ return config.save(b.key(),b.value()); }
  @DeleteMapping("/{key}") Map<String,String> delete(@PathVariable String key){ config.delete(key); return Map.of("message","Deleted."); }
}
