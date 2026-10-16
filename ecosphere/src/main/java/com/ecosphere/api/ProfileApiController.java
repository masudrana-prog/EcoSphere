package com.ecosphere.api;
import com.ecosphere.entity.AppUser; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import java.io.IOException; import java.security.Principal; import java.util.Map;

/** Profile management for every role (tickets 7, 9, 13, 14). */
@RestController @RequestMapping("/api/me") @RequiredArgsConstructor
public class ProfileApiController {
  private final UserLookup lookup; private final ProfileService profiles;
  @GetMapping AppUser me(Principal p){ return profiles.get(lookup.byEmail(p.getName())); }
  @PutMapping AppUser update(@RequestBody Map<String,String> fields,Principal p){ return profiles.update(lookup.byEmail(p.getName()),fields); }
  @PostMapping("/photo") AppUser photo(@RequestParam MultipartFile photo,Principal p) throws IOException { return profiles.uploadPhoto(lookup.byEmail(p.getName()),photo); }
  public record PasswordChange(String current,String next,String confirm) {}
  @PostMapping("/password") Map<String,String> password(@RequestBody PasswordChange b,Principal p){ profiles.changePassword(lookup.byEmail(p.getName()),b.current(),b.next(),b.confirm()); return Map.of("message","Password changed."); }
}
