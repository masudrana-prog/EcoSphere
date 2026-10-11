package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Admin: tree information management (ticket 29). */
@RestController @RequestMapping("/api/admin/species") @RequiredArgsConstructor
public class AdminSpeciesApiController {
  private final UserLookup lookup; private final TreeSpeciesService species;
  @GetMapping List<TreeSpecies> all(){ return species.all(); }
  @PostMapping ResponseEntity<TreeSpecies> create(@RequestBody TreeSpecies s,Principal p){ return ResponseEntity.status(HttpStatus.CREATED).body(species.save(null,s,lookup.byEmail(p.getName()))); }
  @PutMapping("/{id}") TreeSpecies update(@PathVariable Long id,@RequestBody TreeSpecies s,Principal p){ return species.save(id,s,lookup.byEmail(p.getName())); }
  @DeleteMapping("/{id}") Map<String,String> delete(@PathVariable Long id,Principal p){ species.delete(id,lookup.byEmail(p.getName())); return Map.of("message","Species removed."); }
}
