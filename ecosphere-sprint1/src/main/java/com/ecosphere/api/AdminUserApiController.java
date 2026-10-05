package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Admin: user (26), nursery (27) and NGO (28) management. */
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminUserApiController {
  private final UserLookup lookup; private final AdminUserService users; private final NurseryManagementService nurseries; private final NgoManagementService ngos;
  private AppUser me(Principal p){ return lookup.byEmail(p.getName()); }
  public record StatusBody(String status,String reason) {}
  public record PasswordBody(String temporaryPassword) {}

  // ---- all users
  @GetMapping("/users") List<AppUser> list(@RequestParam(required=false) String role,@RequestParam(required=false) String status,@RequestParam(required=false) String q){ return users.list(role,status,q); }
  @GetMapping("/users/counts") Map<String,Long> counts(){ return users.counts(); }
  @GetMapping("/users/{id}") AppUser get(@PathVariable Long id){ return users.get(id); }
  @PostMapping("/users/{id}/approve") AppUser approve(@PathVariable Long id,Principal p){ return users.approve(me(p),id); }
  @PutMapping("/users/{id}/status") AppUser status(@PathVariable Long id,@RequestBody StatusBody b,Principal p){ return users.setStatus(me(p),id,b.status(),b.reason()); }
  @PostMapping("/users/{id}/reset-password") Map<String,String> reset(@PathVariable Long id,@RequestBody PasswordBody b,Principal p){ users.resetPassword(me(p),id,b.temporaryPassword()); return Map.of("message","Password reset."); }
  @DeleteMapping("/users/{id}") Map<String,String> delete(@PathVariable Long id,Principal p){ return Map.of("result",users.delete(me(p),id)); }

  // ---- nurseries
  @GetMapping("/nurseries") List<Map<String,Object>> nurseries(@RequestParam(required=false) String status){ return nurseries.list(status); }
  @GetMapping("/nurseries/{id}") Map<String,Object> nursery(@PathVariable Long id){ return nurseries.detail(id); }
  @PostMapping("/nurseries/{id}/approve") AppUser approveNursery(@PathVariable Long id,Principal p){ return nurseries.approve(me(p),id); }
  @PutMapping("/nurseries/{id}/status") AppUser nurseryStatus(@PathVariable Long id,@RequestBody StatusBody b,Principal p){ return nurseries.setStatus(me(p),id,b.status(),b.reason()); }

  // ---- NGOs
  @GetMapping("/ngos") List<Map<String,Object>> ngos(@RequestParam(required=false) String status){ return ngos.list(status); }
  @GetMapping("/ngos/{id}") Map<String,Object> ngo(@PathVariable Long id){ return ngos.detail(id); }
  @PostMapping("/ngos/{id}/approve") AppUser approveNgo(@PathVariable Long id,Principal p){ return ngos.approve(me(p),id); }
  @PutMapping("/ngos/{id}/status") AppUser ngoStatus(@PathVariable Long id,@RequestBody StatusBody b,Principal p){ return ngos.setStatus(me(p),id,b.status(),b.reason()); }
}
