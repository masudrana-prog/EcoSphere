package com.ecosphere.api;
import com.ecosphere.entity.ActivityLog; import com.ecosphere.service.SystemMonitoringService; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;

/** Plantation activity monitoring (30) and system monitoring & analytics (34). */
@RestController @RequestMapping("/api/admin/system") @RequiredArgsConstructor
public class AdminMonitoringApiController {
  private final SystemMonitoringService monitoring;
  @GetMapping("/health") Map<String,Object> health(){ return monitoring.health(); }
  @GetMapping("/analytics") Map<String,Object> analytics(@RequestParam(defaultValue="14") int days){ return monitoring.analytics(days); }
  @GetMapping("/activity") List<ActivityLog> activity(@RequestParam(required=false) String entityType){ return monitoring.recentActivity(entityType); }
  @GetMapping(value="/activity.csv",produces="text/csv") ResponseEntity<String> csv(){
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=activity.csv").body(monitoring.activityCsv()); }
}
