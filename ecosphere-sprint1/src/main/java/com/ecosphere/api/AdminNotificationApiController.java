package com.ecosphere.api;
import com.ecosphere.entity.Notification; import com.ecosphere.service.NotificationService; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.util.*;

/** Admin: notification management and broadcasts (ticket 33). */
@RestController @RequestMapping("/api/admin/notifications") @RequiredArgsConstructor
public class AdminNotificationApiController {
  private final NotificationService notifications;
  public record BroadcastBody(String role,String title,String message) {}
  @PostMapping("/broadcast") Map<String,Integer> broadcast(@RequestBody BroadcastBody b){ return Map.of("recipients",notifications.broadcast(b.role(),b.title(),b.message())); }
  @GetMapping List<Notification> all(@RequestParam(defaultValue="false") boolean broadcastsOnly){ return broadcastsOnly?notifications.broadcasts():notifications.all(); }
  @DeleteMapping("/{id}") Map<String,String> delete(@PathVariable Long id){ notifications.adminDelete(id); return Map.of("message","Deleted."); }
}
