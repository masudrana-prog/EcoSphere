package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.util.*;

/** Admin: NGO management (ticket 28). */
@Service @RequiredArgsConstructor
public class NgoManagementService {
  private final UserRepo users; private final PlantationRepo plants; private final AdminUserService adminUsers;

  public List<Map<String,Object>> list(String status){
    List<Map<String,Object>> out=new ArrayList<>();
    for(AppUser n:users.findByRoleOrderByCreatedAtDesc(Const.ROLE_NGO)){
      if(status!=null&&!status.isBlank()&&!n.getStatus().equalsIgnoreCase(status)) continue; out.add(row(n)); }
    return out;
  }
  public Map<String,Object> detail(Long id){
    AppUser n=adminUsers.get(id); if(!Const.ROLE_NGO.equals(n.getRole())) throw new NoSuchElementException("NGO not found.");
    Map<String,Object> m=row(n); m.put("recentReviews",plants.findByVerifiedByOrderByVerifiedAtDesc(n).stream().limit(10).toList()); return m;
  }
  public AppUser approve(AppUser admin,Long id){ requireNgo(id); return adminUsers.approve(admin,id); }
  public AppUser setStatus(AppUser admin,Long id,String status,String reason){ requireNgo(id); return adminUsers.setStatus(admin,id,status,reason); }
  private void requireNgo(Long id){ if(!Const.ROLE_NGO.equals(adminUsers.get(id).getRole())) throw new NoSuchElementException("NGO not found."); }
  private Map<String,Object> row(AppUser n){
    long v=plants.countByVerifiedByAndStatus(n,Const.VERIFIED), r=plants.countByVerifiedByAndStatus(n,Const.REJECTED);
    Map<String,Object> m=new LinkedHashMap<>(); m.put("ngo",n); m.put("verified",v); m.put("rejected",r); m.put("approvalRatePercent",(v+r)==0?0:Math.round(v*1000.0/(v+r))/10.0); return m;
  }
}
