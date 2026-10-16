package com.ecosphere.api;
import com.ecosphere.dto.PlantationTracking; import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile;
import java.io.IOException; import java.security.Principal; import java.time.LocalDate; import java.util.*;

/** Plantation submission (5), saved locations (6) and status tracking (10). */
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class UserPlantationApiController {
  private final UserLookup lookup; private final PlantationService plantations; private final PlantationLocationService locations;
  private AppUser me(Principal p){ return lookup.byEmail(p.getName()); }

  @PostMapping(value="/user/plantations",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<Plantation> submit(@RequestParam String treeName,@RequestParam(required=false) String location,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate plantedOn,
      @RequestParam(required=false) Double lat,@RequestParam(required=false) Double lng,@RequestParam(required=false) Long savedLocationId,@RequestParam(required=false) MultipartFile photo,Principal p) throws IOException {
    return ResponseEntity.status(HttpStatus.CREATED).body(plantations.submit(me(p),treeName,location,plantedOn,lat,lng,savedLocationId,photo));
  }
  @GetMapping("/user/plantations") List<Plantation> mine(@RequestParam(required=false) String status,Principal p){ return plantations.mine(me(p),status); }
  @GetMapping("/plantations/{id}/tracking") PlantationTracking tracking(@PathVariable Long id,Principal p){ return plantations.track(id,me(p)); }

  public record LocationBody(String name,String address,String district,Double latitude,Double longitude) {}
  @GetMapping("/user/locations") List<PlantationLocation> locations(Principal p){ return locations.mine(me(p)); }
  @PostMapping("/user/locations") ResponseEntity<PlantationLocation> addLocation(@RequestBody LocationBody b,Principal p){
    return ResponseEntity.status(HttpStatus.CREATED).body(locations.save(me(p),null,b.name(),b.address(),b.district(),b.latitude(),b.longitude())); }
  @PutMapping("/user/locations/{id}") PlantationLocation updateLocation(@PathVariable Long id,@RequestBody LocationBody b,Principal p){ return locations.save(me(p),id,b.name(),b.address(),b.district(),b.latitude(),b.longitude()); }
  @DeleteMapping("/user/locations/{id}") Map<String,String> deleteLocation(@PathVariable Long id,Principal p){ locations.delete(me(p),id); return Map.of("message","Location removed."); }
}
