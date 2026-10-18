package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Complaints & reports: raised by any user, handled by admin (ticket 32). */
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class ReportApiController {
  private final UserLookup lookup; private final ReportService reports;
  public record ReportBody(String type,String subject,String description,Long againstUserId) {}
  @PostMapping("/reports") Report create(@RequestBody ReportBody b,Principal p){ return reports.create(lookup.byEmail(p.getName()),b.type(),b.subject(),b.description(),b.againstUserId()); }
  @GetMapping("/reports") List<Report> mine(Principal p){ return reports.mine(lookup.byEmail(p.getName())); }
  @GetMapping("/reports/against-me") List<Report> aboutMe(Principal p){ return reports.aboutMe(lookup.byEmail(p.getName())); }

  public record HandleBody(String status,String response) {}
  @GetMapping("/admin/reports") List<Report> all(@RequestParam(required=false) String status){ return reports.adminList(status); }
  @GetMapping("/admin/reports/counts") Map<String,Long> counts(){ return reports.counts(); }
  @GetMapping("/admin/reports/{id}") Report get(@PathVariable Long id){ return reports.get(id); }
  @PostMapping("/admin/reports/{id}/handle") Report handle(@PathVariable Long id,@RequestBody HandleBody b,Principal p){ return reports.handle(id,lookup.byEmail(p.getName()),b.status(),b.response()); }
}
