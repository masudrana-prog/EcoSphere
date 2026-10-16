package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Admin: EcoRewards and badge management (tickets 12, 31). */
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminRewardApiController {
  private final UserLookup lookup; private final BadgeService badges; private final EcoPointsService points;
  private AppUser me(Principal p){ return lookup.byEmail(p.getName()); }

  public record BadgeBody(String name,String description,String icon,String criteriaType,int threshold,int bonusPoints,Boolean active) {}
  @GetMapping("/badges") List<Map<String,Object>> badges(){
    List<Map<String,Object>> out=new ArrayList<>(); for(Badge b:badges.all()){ Map<String,Object> row=new LinkedHashMap<>(); row.put("badge",b); row.put("holders",badges.holders(b)); out.add(row); } return out; }
  @PostMapping("/badges") ResponseEntity<Badge> createBadge(@RequestBody BadgeBody b,Principal p){
    return ResponseEntity.status(HttpStatus.CREATED).body(badges.save(null,b.name(),b.description(),b.icon(),b.criteriaType(),b.threshold(),b.bonusPoints(),b.active()==null||b.active(),me(p))); }
  @PutMapping("/badges/{id}") Badge updateBadge(@PathVariable Long id,@RequestBody BadgeBody b,Principal p){
    return badges.save(id,b.name(),b.description(),b.icon(),b.criteriaType(),b.threshold(),b.bonusPoints(),b.active()==null||b.active(),me(p)); }
  @DeleteMapping("/badges/{id}") Map<String,String> deleteBadge(@PathVariable Long id,Principal p){ badges.delete(id,me(p)); return Map.of("message","Badge deleted."); }

  public record RewardBody(String name,String description,int pointsCost,int stock,Boolean active) {}
  @GetMapping("/rewards") List<Reward> rewards(){ return points.allRewards(); }
  @PostMapping("/rewards") ResponseEntity<Reward> createReward(@RequestBody RewardBody b,Principal p){
    return ResponseEntity.status(HttpStatus.CREATED).body(points.saveReward(null,b.name(),b.description(),b.pointsCost(),b.stock(),b.active()==null||b.active(),me(p))); }
  @PutMapping("/rewards/{id}") Reward updateReward(@PathVariable Long id,@RequestBody RewardBody b,Principal p){
    return points.saveReward(id,b.name(),b.description(),b.pointsCost(),b.stock(),b.active()==null||b.active(),me(p)); }
  @DeleteMapping("/rewards/{id}") Map<String,String> deleteReward(@PathVariable Long id,Principal p){ points.deleteReward(id,me(p)); return Map.of("message","Reward removed."); }
  @GetMapping("/rewards/redemptions") List<RewardRedemption> redemptions(){ return points.allRedemptions(); }
}
