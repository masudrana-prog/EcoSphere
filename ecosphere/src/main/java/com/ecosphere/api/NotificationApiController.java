package com.ecosphere.api;
import com.ecosphere.entity.Notification; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Notifications for every logged-in role (ticket 23). */
@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationApiController {
  private final UserLookup lookup; private final NotificationService notifications;
  @GetMapping List<Notification> mine(Principal p){ return notifications.mine(lookup.byEmail(p.getName())); }
  @GetMapping("/unread-count") Map<String,Long> unread(Principal p){ return Map.of("unread",notifications.unreadCount(lookup.byEmail(p.getName()))); }
  @PostMapping("/{id}/read") Notification read(@PathVariable Long id,Principal p){ return notifications.markRead(id,lookup.byEmail(p.getName())); }
  @PostMapping("/read-all") Map<String,Integer> readAll(Principal p){ return Map.of("marked",notifications.markAllRead(lookup.byEmail(p.getName()))); }
  @DeleteMapping("/{id}") Map<String,String> delete(@PathVariable Long id,Principal p){ notifications.delete(id,lookup.byEmail(p.getName())); return Map.of("message","Deleted."); }
}
