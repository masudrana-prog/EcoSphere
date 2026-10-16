package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** Admin: user management for every role (ticket 26). */
@Service @RequiredArgsConstructor
public class AdminUserService {
  private static final Set<String> STATUSES=Set.of(Const.ACTIVE,Const.PENDING,Const.SUSPENDED);
  private final UserRepo users; private final PlantationRepo plants; private final TreeOrderRepo orders; private final ConsumptionRepo consumption;
  private final PasswordEncoder encoder; private final NotificationService notifications; private final ActivityLogService log;

  public List<AppUser> list(String role,String status,String q){
    List<AppUser> base=(q!=null&&!q.isBlank())?users.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q.trim(),q.trim()):users.findAll();
    return base.stream().filter(u->role==null||role.isBlank()||u.getRole().equalsIgnoreCase(role)).filter(u->status==null||status.isBlank()||u.getStatus().equalsIgnoreCase(status))
      .sorted(Comparator.comparing(AppUser::getCreatedAt).reversed()).toList();
  }
  public AppUser get(Long id){ return users.findById(id).orElseThrow(()->new NoSuchElementException("User not found.")); }

  @Transactional
  public AppUser setStatus(AppUser admin,Long id,String status,String reason){
    String s=status==null?"":status.trim().toUpperCase();
    if(!STATUSES.contains(s)) throw new IllegalArgumentException("Status must be one of "+STATUSES);
    AppUser u=get(id);
    if(u.getId().equals(admin.getId())) throw new IllegalArgumentException("You cannot change your own account status.");
    String before=u.getStatus(); u.setStatus(s); users.save(u);
    notifications.notify(u,"Account "+s.toLowerCase(),
      Const.PENDING.equals(before)&&Const.ACTIVE.equals(s)?"Your account was approved. You can now use EcoSphere.":"Your account status is now "+s+(reason==null||reason.isBlank()?".":": "+reason),"INFO",null);
    log.log(admin,"USER_"+s,"User",u.getId(),u.getEmail()+(reason==null?"":" - "+reason)); return u;
  }
  public AppUser approve(AppUser admin,Long id){
    if(!Const.PENDING.equals(get(id).getStatus())) throw new IllegalArgumentException("Only pending accounts can be approved.");
    return setStatus(admin,id,Const.ACTIVE,null);
  }
  /** Sets a temporary password the user should change after logging in. */
  @Transactional
  public void resetPassword(AppUser admin,Long id,String temporary){
    if(temporary==null||temporary.length()<8) throw new IllegalArgumentException("Temporary password must be at least 8 characters.");
    AppUser u=get(id); u.setPasswordHash(encoder.encode(temporary)); users.save(u);
    notifications.notify(u,"Password reset","An administrator reset your password. Please change it after logging in.","INFO","/profile");
    log.log(admin,"PASSWORD_RESET","User",id,u.getEmail());
  }
  /** Accounts with history are suspended instead of deleted so plantation / order records stay valid. */
  @Transactional
  public String delete(AppUser admin,Long id){
    AppUser u=get(id);
    if(u.getId().equals(admin.getId())) throw new IllegalArgumentException("You cannot delete your own account.");
    boolean hasHistory=plants.countByUser(u)>0||plants.countByVerifiedBy(u)>0||consumption.countByUser(u)>0||!orders.findByBuyerOrderByCreatedAtDesc(u).isEmpty()||!orders.findByNurseryOrderByCreatedAtDesc(u).isEmpty();
    if(hasHistory){ u.setStatus(Const.SUSPENDED); users.save(u); log.log(admin,"USER_SUSPENDED_INSTEAD_OF_DELETE","User",id,u.getEmail()); return "SUSPENDED"; }
    users.delete(u); log.log(admin,"USER_DELETED","User",id,u.getEmail()); return "DELETED";
  }
  public Map<String,Long> counts(){
    Map<String,Long> m=new LinkedHashMap<>();
    for(String r:List.of(Const.ROLE_USER,Const.ROLE_NGO,Const.ROLE_NURSERY,Const.ROLE_ADMIN)) m.put(r,users.countByRole(r)); m.put("TOTAL",users.count()); return m;
  }
}
