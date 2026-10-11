package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** User side of EcoPoints, badges and rewards (tickets 12, 31). */
@RestController @RequestMapping("/api/user") @RequiredArgsConstructor
public class UserRewardApiController {
  private final UserLookup lookup; private final BadgeService badges; private final EcoPointsService points;
  @GetMapping("/badges") List<UserBadge> myBadges(Principal p){ return badges.earned(lookup.byEmail(p.getName())); }
  @PostMapping("/badges/evaluate") List<Badge> evaluate(Principal p){ return badges.evaluate(lookup.byEmail(p.getName())); }
  @GetMapping("/rewards") List<Reward> rewards(){ return points.catalog(); }
  @PostMapping("/rewards/{id}/redeem") RewardRedemption redeem(@PathVariable Long id,Principal p){ return points.redeem(lookup.byEmail(p.getName()),id); }
  @GetMapping("/rewards/redemptions") List<RewardRedemption> redemptions(Principal p){ return points.myRedemptions(lookup.byEmail(p.getName())); }
}
