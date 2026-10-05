package com.ecosphere.service;
import com.ecosphere.entity.AppUser; import com.ecosphere.repository.UserRepo; import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; import java.io.IOException; import java.util.Map;

/** Profile management shared by User, NGO, Nursery and Admin (tickets 7, 9, 13, 14). */
@Service @RequiredArgsConstructor
public class ProfileService {
  private final UserRepo users; private final PasswordEncoder encoder; private final FileStorageService files; private final ActivityLogService log;

  public AppUser get(AppUser me){ return users.findById(me.getId()).orElseThrow(); }

  /** Only the supplied (non-null) fields are changed. Role, e-mail, status and points can never be edited here. */
  public AppUser update(AppUser me,Map<String,String> f){
    AppUser u=get(me);
    if(f.containsKey("name")){ if(f.get("name")==null||f.get("name").isBlank()) throw new IllegalArgumentException("Name cannot be empty."); u.setName(f.get("name").trim()); }
    if(f.containsKey("phone")){ if(f.get("phone")==null||f.get("phone").isBlank()) throw new IllegalArgumentException("Phone cannot be empty."); u.setPhone(f.get("phone").trim()); }
    if(f.containsKey("location")) u.setLocation(f.get("location"));
    if(f.containsKey("bio")){ if(f.get("bio")!=null&&f.get("bio").length()>1000) throw new IllegalArgumentException("Bio is limited to 1000 characters."); u.setBio(f.get("bio")); }
    if(f.containsKey("address")) u.setAddress(f.get("address"));
    if(f.containsKey("website")) u.setWebsite(f.get("website"));
    if(!"USER".equals(u.getRole())){ // organisation style fields
      if(f.containsKey("organizationName")) u.setOrganizationName(f.get("organizationName"));
      if(f.containsKey("registrationNo")) u.setRegistrationNo(f.get("registrationNo"));
    }
    if("ADMIN".equals(u.getRole())&&f.containsKey("adminDepartment")) u.setAdminDepartment(f.get("adminDepartment"));
    u=users.save(u); log.log(u,"PROFILE_UPDATED","User",u.getId(),null); return u;
  }
  public AppUser uploadPhoto(AppUser me,MultipartFile photo) throws IOException {
    String url=files.storeImage(photo); if(url==null) throw new IllegalArgumentException("Choose an image to upload.");
    AppUser u=get(me); u.setProfileImageUrl(url); return users.save(u);
  }
  public void changePassword(AppUser me,String current,String next,String confirm){
    AppUser u=get(me);
    if(current==null||!encoder.matches(current,u.getPasswordHash())) throw new IllegalArgumentException("Current password is incorrect.");
    if(next==null||next.length()<8) throw new IllegalArgumentException("New password must be at least 8 characters.");
    if(!next.equals(confirm)) throw new IllegalArgumentException("Passwords do not match.");
    if(encoder.matches(next,u.getPasswordHash())) throw new IllegalArgumentException("New password must differ from the current one.");
    u.setPasswordHash(encoder.encode(next)); users.save(u); log.log(u,"PASSWORD_CHANGED","User",u.getId(),null);
  }
}
