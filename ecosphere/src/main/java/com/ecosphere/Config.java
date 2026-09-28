package com.ecosphere;
import org.springframework.context.annotation.*; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service; import org.springframework.web.servlet.config.annotation.*;
@Configuration @EnableWebSecurity
class SecurityConfig implements WebMvcConfigurer {
  @Bean PasswordEncoder encoder(){ return new BCryptPasswordEncoder(); }
  @Override public void addResourceHandlers(ResourceHandlerRegistry r){ r.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/"); }
  @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(a->a
      .requestMatchers("/login","/register","/css/**","/uploads/**").permitAll()
      .requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/ngo/**").hasRole("NGO")
      .requestMatchers("/nursery/**").hasRole("NURSERY").requestMatchers("/user/**").hasRole("USER")
      .anyRequest().authenticated())
    .formLogin(f->f.loginPage("/login").usernameParameter("email").failureUrl("/login?error")
      .successHandler((q,s,auth)->s.sendRedirect(home(auth.getAuthorities().iterator().next().getAuthority()))))
    .logout(l->l.logoutSuccessUrl("/login?logout"));
    return http.build();
  }
  static String home(String r){ return switch(r){ case "ROLE_ADMIN"->"/admin"; case "ROLE_NGO"->"/ngo"; case "ROLE_NURSERY"->"/nursery"; default->"/user"; }; }
}
@Service
class AppUserDetailsService implements UserDetailsService {
  private final UserRepo repo; AppUserDetailsService(UserRepo r){repo=r;}
  public UserDetails loadUserByUsername(String email){
    AppUser u=repo.findByEmail(email.trim().toLowerCase()).orElseThrow(()->new UsernameNotFoundException(email));
    return User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles(u.getRole()).disabled(!"ACTIVE".equals(u.getStatus())).build();
  }
}
