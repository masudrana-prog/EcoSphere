package com.ecosphere;
import org.springframework.web.bind.annotation.ModelAttribute; import java.security.Principal;
@org.springframework.web.bind.annotation.ControllerAdvice
class CurrentUserAdvice {
  UserRepo users; CurrentUserAdvice(UserRepo u){users=u;}
  @ModelAttribute("me") AppUser me(Principal p){ return p==null?null:users.findByEmail(p.getName()).orElse(null); }
}
