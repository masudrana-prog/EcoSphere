package com.ecosphere.config;
import com.ecosphere.entity.AppUser; import com.ecosphere.repository.UserRepo; import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*; import org.springframework.stereotype.Service;

/** Loads accounts for Spring Security; only ACTIVE accounts can log in (PENDING / SUSPENDED are "disabled"). */
@Service @RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepo repo;
  @Override public UserDetails loadUserByUsername(String email){
    AppUser u=repo.findByEmail(email.trim().toLowerCase()).orElseThrow(()->new UsernameNotFoundException(email));
    return User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles(u.getRole()).disabled(!"ACTIVE".equals(u.getStatus())).build();
  }
}
