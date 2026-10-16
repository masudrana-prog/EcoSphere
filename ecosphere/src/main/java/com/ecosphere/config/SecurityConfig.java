package com.ecosphere.config;
import com.ecosphere.service.AuthService; import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*; import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/** Two chains: /api/** uses HTTP Basic (stateless, JSON), everything else uses the form login of the Thymeleaf UI. */
@Configuration @EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {
  private final AuthService authService;

  @Bean @Order(1)
  SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**")
      .csrf(c->c.disable()) // credentials are sent per request via Basic auth, no cookie session to forge
      .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .httpBasic(Customizer.withDefaults())
      .authorizeHttpRequests(a->a
        .requestMatchers("/api/auth/register").permitAll()
        .requestMatchers("/api/species/**","/api/catalogue/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/ngo/**").hasRole("NGO")
        .requestMatchers("/api/nursery/**").hasRole("NURSERY")
        .requestMatchers("/api/user/**").hasRole("USER")
        .anyRequest().authenticated());
    return http.build();
  }

  @Bean @Order(2)
  SecurityFilterChain web(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(a->a
      .requestMatchers("/login","/register","/css/**","/uploads/**").permitAll()
      .requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/ngo/**").hasRole("NGO")
      .requestMatchers("/nursery/**").hasRole("NURSERY").requestMatchers("/user/**").hasRole("USER")
      .anyRequest().authenticated())
    .formLogin(f->f.loginPage("/login").usernameParameter("email").failureUrl("/login?error")
      .successHandler((q,s,auth)->{ authService.recordLogin(auth.getName()); s.sendRedirect(home(auth.getAuthorities().iterator().next().getAuthority())); }))
    .logout(l->l.logoutSuccessUrl("/login?logout"));
    return http.build();
  }
  public static String home(String r){ return switch(r){ case "ROLE_ADMIN"->"/admin"; case "ROLE_NGO"->"/ngo"; case "ROLE_NURSERY"->"/nursery"; default->"/user"; }; }
}
