package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Admin: plantation submission management and monitoring (tickets 2, 30). */
@RestController @RequestMapping("/api/admin/plantations") @RequiredArgsConstructor
public class AdminPlantationApiController {
  private final UserLookup lookup; private final PlantationService plantations;
  private AppUser me(Principal p){ return lookup.byEmail(p.getName()); }
  @GetMapping List<Plantation> list(@RequestParam(required=false) String status){ return plantations.adminList(status); }
  @GetMapping("/summary") Map<String,Long> summary(){ return plantations.statusCounts(); }
  @GetMapping("/{id}") Plantation get(@PathVariable Long id){ return plantations.get(id); }
  @PostMapping("/{id}/approve") Plantation approve(@PathVariable Long id,Principal p){ return plantations.approve(id,me(p)); }
  public record ReasonBody(String reason) {}
  @PostMapping("/{id}/reject") Plantation reject(@PathVariable Long id,@RequestBody ReasonBody b,Principal p){ return plantations.reject(id,me(p),b.reason()); }
  @PostMapping("/{id}/reopen") Plantation reopen(@PathVariable Long id,@RequestBody(required=false) ReasonBody b,Principal p){ return plantations.adminReopen(id,me(p),b==null?null:b.reason()); }
  @DeleteMapping("/{id}") Map<String,String> delete(@PathVariable Long id,Principal p){ plantations.adminDelete(id,me(p)); return Map.of("message","Plantation deleted."); }
}
