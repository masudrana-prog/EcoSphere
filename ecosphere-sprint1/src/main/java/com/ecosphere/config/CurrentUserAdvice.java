package com.ecosphere.config;
import com.ecosphere.entity.AppUser; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*; import java.security.Principal;

/** Adds the logged-in user ("me") and unread notification count to every Thymeleaf model. */
@ControllerAdvice(basePackages="com.ecosphere.controller") @RequiredArgsConstructor
public class CurrentUserAdvice {
  private final UserRepo users; private final NotificationRepo notifications;
  @ModelAttribute("me") public AppUser me(Principal p){ return p==null?null:users.findByEmail(p.getName()).orElse(null); }
  @ModelAttribute("unread") public Long unread(Principal p){
    if(p==null) return 0L; return users.findByEmail(p.getName()).map(notifications::countByRecipientAndSeenFalse).orElse(0L);
  }
}
