package com.ecosphere.api;
import com.ecosphere.dto.RegisterRequest; import com.ecosphere.entity.AppUser; import com.ecosphere.service.AuthService; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;

/** Registration for User / NGO / Nursery / Admin (tickets 8, 1, 3, 4). Login is HTTP Basic on every other /api call. */
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthApiController {
  private final AuthService auth;
  @PostMapping("/register") ResponseEntity<Map<String,Object>> register(@RequestBody RegisterRequest r){
    AppUser u=auth.register(r);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",u.getId(),"email",u.getEmail(),"role",u.getRole(),"status",u.getStatus(),
      "message","PENDING".equals(u.getStatus())?"Registered. An administrator must approve the account before you can log in.":"Registered. You can log in now."));
  }
}
