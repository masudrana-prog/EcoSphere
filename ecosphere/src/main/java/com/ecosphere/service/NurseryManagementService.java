package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.util.*;

/** Admin: nursery management (ticket 27). */
@Service @RequiredArgsConstructor
public class NurseryManagementService {
  private final UserRepo users; private final TreeRepo trees; private final TreeOrderRepo orders; private final PlantationRequestRepo requests; private final AdminUserService adminUsers;

  public List<Map<String,Object>> list(String status){
    List<Map<String,Object>> out=new ArrayList<>();
    for(AppUser n:users.findByRoleOrderByCreatedAtDesc(Const.ROLE_NURSERY)){
      if(status!=null&&!status.isBlank()&&!n.getStatus().equalsIgnoreCase(status)) continue;
      out.add(row(n)); }
    return out;
  }
  public Map<String,Object> detail(Long id){
    AppUser n=adminUsers.get(id); if(!Const.ROLE_NURSERY.equals(n.getRole())) throw new NoSuchElementException("Nursery not found.");
    Map<String,Object> m=row(n); m.put("trees",trees.findByNurseryOrderByIdDesc(n)); m.put("recentOrders",orders.findByNurseryOrderByCreatedAtDesc(n).stream().limit(10).toList()); return m;
  }
  public AppUser approve(AppUser admin,Long id){ requireNursery(id); return adminUsers.approve(admin,id); }
  public AppUser setStatus(AppUser admin,Long id,String status,String reason){ requireNursery(id); return adminUsers.setStatus(admin,id,status,reason); }
  private void requireNursery(Long id){ if(!Const.ROLE_NURSERY.equals(adminUsers.get(id).getRole())) throw new NoSuchElementException("Nursery not found."); }
  private Map<String,Object> row(AppUser n){
    List<Tree> ts=trees.findByNurseryOrderByIdDesc(n); Map<String,Object> m=new LinkedHashMap<>();
    m.put("nursery",n); m.put("treeLines",ts.size()); m.put("unitsInStock",ts.stream().mapToInt(Tree::getQuantity).sum());
    m.put("lowStockLines",ts.stream().filter(Tree::isLowStock).count()); m.put("pendingOrders",orders.countByNurseryAndStatus(n,Const.PENDING));
    m.put("deliveredRevenue",orders.revenue(n,Const.DELIVERED)); m.put("newRequests",requests.countByNurseryAndStatus(n,Const.REQ_REQUESTED)); return m;
  }
}
