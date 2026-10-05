package com.ecosphere.service;
import com.ecosphere.entity.AppUser; import com.ecosphere.repository.UserRepo; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component; import java.util.NoSuchElementException;

/** Resolves the logged-in user (principal name is the e-mail). */
@Component @RequiredArgsConstructor
public class UserLookup {
  private final UserRepo users;
  public AppUser byEmail(String email){ return users.findByEmail(email.trim().toLowerCase()).orElseThrow(()->new NoSuchElementException("User not found.")); }
  public AppUser byId(Long id){ return users.findById(id).orElseThrow(()->new NoSuchElementException("User not found.")); }
}
