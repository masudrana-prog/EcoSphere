package com.ecosphere;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*; import java.util.Set;
@Controller
class AuthController {
  UserRepo users; PasswordEncoder pe; AuthController(UserRepo u,PasswordEncoder p){users=u;pe=p;}
  @GetMapping("/") String root(org.springframework.security.core.Authentication a){ return a==null||a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken?"redirect:/login":"redirect:"+SecurityConfig.home(a.getAuthorities().iterator().next().getAuthority()); }
  @GetMapping("/login") String login(){ return "login"; }
  @GetMapping("/register") String reg(){ return "register"; }
  @PostMapping("/register")
  String doReg(@RequestParam String name,@RequestParam String email,@RequestParam String phone,@RequestParam(defaultValue="") String location,
      @RequestParam String password,@RequestParam String confirm,@RequestParam(defaultValue="USER") String role,@RequestParam(required=false) String terms,Model m){
    String err=null; email=email.trim().toLowerCase();
    if(terms==null) err="Please accept the Terms & Sustainability Charter.";
    else if(password.length()<8) err="Password must be at least 8 characters.";
    else if(!password.equals(confirm)) err="Passwords do not match.";
    else if(users.findByEmail(email).isPresent()) err="An account with this email already exists.";
    else if(!Set.of("USER","NGO","NURSERY","ADMIN").contains(role)) err="Invalid role.";
    if(err!=null){ m.addAttribute("error",err); return "register"; }
    AppUser u=new AppUser(); u.setName(name);u.setEmail(email);u.setPhone(phone);u.setLocation(location);u.setRole(role);
    u.setStatus("USER".equals(role)?"ACTIVE":"PENDING"); u.setPasswordHash(pe.encode(password)); users.save(u);
    return "redirect:/login?registered"+("USER".equals(role)?"":"&pending");
  }
}
