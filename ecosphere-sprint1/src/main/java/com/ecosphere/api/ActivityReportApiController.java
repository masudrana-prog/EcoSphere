package com.ecosphere.api;
import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.Map;

/** Role specific activity reports (ticket 24). */
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class ActivityReportApiController {
  private final UserLookup lookup; private final ActivityReportService reports;
  @GetMapping("/ngo/report") Map<String,Object> ngo(Principal p){ return reports.ngoReport(lookup.byEmail(p.getName())); }
  @GetMapping("/nursery/report") Map<String,Object> nursery(Principal p){ return reports.nurseryReport(lookup.byEmail(p.getName())); }
  @GetMapping("/user/report") Map<String,Object> user(Principal p){ return reports.userReport(lookup.byEmail(p.getName())); }
}
