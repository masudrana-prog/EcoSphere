package com.ecosphere.controller;
import com.ecosphere.config.SecurityConfig; import com.ecosphere.dto.RegisterRequest; import com.ecosphere.service.AuthService; import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken; import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*;

/** Login / registration pages for all roles (tickets 1, 3, 4, 8). */
@Controller @RequiredArgsConstructor
public class AuthController {
  private final AuthService auth;
  @GetMapping("/") String root(Authentication a){ return a==null||a instanceof AnonymousAuthenticationToken?"redirect:/login":"redirect:"+SecurityConfig.home(a.getAuthorities().iterator().next().getAuthority()); }
  @GetMapping("/login") String login(){ return "login"; }
  @GetMapping("/register") String reg(){ return "register"; }
  @PostMapping("/register")
  String doReg(@RequestParam String name,@RequestParam String email,@RequestParam String phone,@RequestParam(defaultValue="") String location,
      @RequestParam String password,@RequestParam String confirm,@RequestParam(defaultValue="USER") String role,@RequestParam(required=false) String terms,
      @RequestParam(required=false) String organizationName,@RequestParam(required=false) String registrationNo,Model m){
    try{ auth.register(new RegisterRequest(name,email,phone,location,password,confirm,role,terms!=null,organizationName,registrationNo)); }
    catch(IllegalArgumentException e){ m.addAttribute("error",e.getMessage()); return "register"; }
    return "redirect:/login?registered"+("USER".equals(role)?"":"&pending");
  }
}
