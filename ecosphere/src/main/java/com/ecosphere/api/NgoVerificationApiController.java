package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** NGO plantation verification (ticket 11). */
@RestController @RequestMapping("/api/ngo/plantations") @RequiredArgsConstructor
public class NgoVerificationApiController {
  private final UserLookup lookup; private final PlantationService plantations;
  @GetMapping("/pending") List<Plantation> queue(){ return plantations.pendingQueue(); }
  @GetMapping("/reviewed") List<Plantation> history(@RequestParam(defaultValue="false") boolean mineOnly,Principal p){ return mineOnly?plantations.reviewedBy(lookup.byEmail(p.getName())):plantations.reviewed(); }
  @GetMapping("/{id}") Plantation detail(@PathVariable Long id){ return plantations.get(id); }
  @PostMapping("/{id}/approve") Plantation approve(@PathVariable Long id,Principal p){ return plantations.approve(id,lookup.byEmail(p.getName())); }
  public record RejectBody(String reason) {}
  @PostMapping("/{id}/reject") Plantation reject(@PathVariable Long id,@RequestBody RejectBody b,Principal p){ return plantations.reject(id,lookup.byEmail(p.getName()),b.reason()); }
}
