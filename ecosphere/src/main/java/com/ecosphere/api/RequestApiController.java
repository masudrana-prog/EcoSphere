package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Sapling requests: users / NGOs ask, nurseries respond, admin oversees (ticket 22). */
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class RequestApiController {
  private final UserLookup lookup; private final PlantationRequestService requests;
  private AppUser requester(Principal p){ AppUser u=lookup.byEmail(p.getName()); if(!"USER".equals(u.getRole())&&!"NGO".equals(u.getRole())) throw new SecurityException("Only users and NGOs can request saplings."); return u; }

  public record SaplingRequest(Long treeId,int quantity,String message) {}
  @PostMapping("/requests") PlantationRequest create(@RequestBody SaplingRequest b,Principal p){ return requests.create(requester(p),b.treeId(),b.quantity(),b.message()); }
  @GetMapping("/requests") List<PlantationRequest> mine(Principal p){ return requests.mine(requester(p)); }
  @PostMapping("/requests/{id}/cancel") PlantationRequest cancel(@PathVariable Long id,Principal p){ return requests.cancel(requester(p),id); }

  public record RespondBody(String action,String response) {}
  @GetMapping("/nursery/requests") List<PlantationRequest> incoming(Principal p){ return requests.forNursery(lookup.byEmail(p.getName())); }
  @PostMapping("/nursery/requests/{id}/respond") PlantationRequest respond(@PathVariable Long id,@RequestBody RespondBody b,Principal p){ return requests.respond(lookup.byEmail(p.getName()),id,b.action(),b.response()); }

  @GetMapping("/admin/requests") List<PlantationRequest> all(){ return requests.all(); }
}
