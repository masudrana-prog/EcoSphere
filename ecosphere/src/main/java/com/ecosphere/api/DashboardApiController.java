package com.ecosphere.api;
import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.Map;

/** Sustainability dashboard (ticket 19) and admin overview (ticket 25). */
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class DashboardApiController {
  private final UserLookup lookup; private final DashboardService dashboard;
  @GetMapping("/user/dashboard") Map<String,Object> user(Principal p){ return dashboard.userDashboard(lookup.byEmail(p.getName())); }
  @GetMapping("/admin/overview") Map<String,Object> admin(){ return dashboard.adminOverview(); }
}
