package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.dto.RegisterRequest; import com.ecosphere.entity.AppUser; import com.ecosphere.repository.UserRepo;
import lombok.RequiredArgsConstructor; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service;
import java.time.LocalDateTime; import java.util.Set; import java.util.regex.Pattern;

/** Registration and login bookkeeping for all four roles (tickets 1, 3, 4, 8). */
@Service @RequiredArgsConstructor
public class AuthService {
  private static final Pattern EMAIL=Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final Set<String> ROLES=Set.of(Const.ROLE_USER,Const.ROLE_NGO,Const.ROLE_NURSERY,Const.ROLE_ADMIN);
  private final UserRepo users; private final PasswordEncoder encoder; private final NotificationService notifications; private final ActivityLogService log;

  /** USER accounts are ACTIVE at once; NGO / NURSERY / ADMIN accounts wait for admin approval. */
  public AppUser register(RegisterRequest r){
    String email=r.email()==null?"":r.email().trim().toLowerCase();
    String role=r.role()==null||r.role().isBlank()?Const.ROLE_USER:r.role().trim().toUpperCase();
    if(!r.termsAccepted()) throw new IllegalArgumentException("Please accept the Terms & Sustainability Charter.");
    if(r.name()==null||r.name().isBlank()) throw new IllegalArgumentException("Name is required.");
    if(!EMAIL.matcher(email).matches()) throw new IllegalArgumentException("Enter a valid e-mail address.");
    if(r.phone()==null||r.phone().isBlank()) throw new IllegalArgumentException("Phone number is required.");
    if(r.password()==null||r.password().length()<8) throw new IllegalArgumentException("Password must be at least 8 characters.");
    if(!r.password().equals(r.confirm())) throw new IllegalArgumentException("Passwords do not match.");
    if(!ROLES.contains(role)) throw new IllegalArgumentException("Invalid role.");
    if(users.findByEmail(email).isPresent()) throw new IllegalArgumentException("An account with this email already exists.");
    if((Const.ROLE_NGO.equals(role)||Const.ROLE_NURSERY.equals(role))&&(r.organizationName()==null||r.organizationName().isBlank()))
      throw new IllegalArgumentException("Organisation name is required for NGO and Nursery accounts.");

    AppUser u=new AppUser(); u.setName(r.name().trim()); u.setEmail(email); u.setPhone(r.phone().trim());
    u.setLocation(r.location()==null?"":r.location().trim()); u.setRole(role);
    u.setOrganizationName(r.organizationName()); u.setRegistrationNo(r.registrationNo());
    u.setStatus(Const.ROLE_USER.equals(role)?Const.ACTIVE:Const.PENDING); u.setPasswordHash(encoder.encode(r.password()));
    u=users.save(u);
    log.log(u,"REGISTERED","User",u.getId(),role);
    if(Const.PENDING.equals(u.getStatus()))
      notifications.notifyRole(Const.ROLE_ADMIN,"Approval needed",u.getName()+" registered as "+role+" and awaits approval.","INFO","/admin");
    return u;
  }
  public void recordLogin(String email){
    users.findByEmail(email).ifPresent(u->{ u.setLastLoginAt(LocalDateTime.now()); users.save(u); log.log(u,"LOGIN","User",u.getId(),null); });
  }
}
