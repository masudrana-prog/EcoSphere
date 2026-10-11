package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** Availability, low-stock and restock handling for nurseries (ticket 21). */
@Service @RequiredArgsConstructor
public class NurseryAvailabilityService {
  private final TreeRepo trees; private final TreeService treeService; private final NotificationService notifications; private final ConfigService config;

  @Transactional
  public Tree setListed(AppUser nursery,Long id,boolean listed){ Tree t=treeService.owned(nursery,id); t.setListed(listed); return trees.save(t); }

  @Transactional
  public Tree setThreshold(AppUser nursery,Long id,int threshold){
    if(threshold<0) throw new IllegalArgumentException("Threshold cannot be negative.");
    Tree t=treeService.owned(nursery,id); t.setLowStockThreshold(threshold); return trees.save(t);
  }
  /** Adds saplings to stock (delta>0); warns the nursery when the line is still below the threshold. */
  @Transactional
  public Tree restock(AppUser nursery,Long id,int add){
    if(add<=0) throw new IllegalArgumentException("Restock amount must be positive.");
    Tree t=trees.lockById(treeService.owned(nursery,id).getId()).orElseThrow();
    t.setQuantity(t.getQuantity()+add); return trees.save(t);
  }
  /** Overview used by the nursery dashboard: available / low / out of stock lines. */
  public Map<String,Object> summary(AppUser nursery){
    List<Tree> list=trees.findByNurseryOrderByIdDesc(nursery);
    List<Tree> low=list.stream().filter(Tree::isLowStock).toList(), out=list.stream().filter(t->t.getQuantity()==0).toList();
    Map<String,Object> m=new LinkedHashMap<>();
    m.put("lines",list.size()); m.put("totalUnits",list.stream().mapToInt(Tree::getQuantity).sum());
    m.put("available",list.stream().filter(Tree::isAvailable).count()); m.put("lowStock",low); m.put("outOfStock",out);
    m.put("hidden",list.stream().filter(t->!t.isListed()).count()); return m;
  }
  /** Public availability of one nursery (what buyers see). */
  public List<Tree> publicAvailability(AppUser nursery){ return trees.findByNurseryOrderByIdDesc(nursery).stream().filter(Tree::isAvailable).toList(); }

  /** Sends one low-stock notification per line that is below its threshold. */
  public int sendLowStockAlerts(AppUser nursery){
    int n=0; for(Tree t:trees.findByNurseryOrderByIdDesc(nursery)) if(t.isLowStock()){
      notifications.notify(nursery,"Low stock: "+t.getName(),"Only "+t.getQuantity()+" units left (threshold "+t.getLowStockThreshold()+").","INFO","/nursery"); n++; }
    return n;
  }
  public int defaultThreshold(){ return config.getInt("LOW_STOCK_THRESHOLD",100); }
}
