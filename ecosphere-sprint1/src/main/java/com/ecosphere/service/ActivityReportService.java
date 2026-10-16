package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.util.*;

/** Role specific activity reports for NGOs, nurseries and users (ticket 24). */
@Service @RequiredArgsConstructor
public class ActivityReportService {
  private final PlantationRepo plants; private final TreeOrderRepo orders; private final PlantationRequestRepo requests; private final TreeRepo trees;
  private final ConsumptionRepo consumption; private final ReportRepo reports; private final ActivityLogService log; private final CarbonService carbon;

  public Map<String,Object> ngoReport(AppUser ngo){
    long verified=plants.countByVerifiedByAndStatus(ngo,Const.VERIFIED), rejected=plants.countByVerifiedByAndStatus(ngo,Const.REJECTED), done=verified+rejected;
    Map<String,Object> m=new LinkedHashMap<>();
    m.put("pendingQueue",plants.countByStatus(Const.PENDING)); m.put("verifiedByMe",verified); m.put("rejectedByMe",rejected);
    m.put("approvalRatePercent",done==0?0:Math.round(verified*1000.0/done)/10.0);
    m.put("ecoPointsAwarded",plants.findByVerifiedByOrderByVerifiedAtDesc(ngo).stream().mapToInt(Plantation::getPointsAwarded).sum());
    m.put("recentActivity",log.byActor(ngo.getEmail())); return m;
  }
  public Map<String,Object> nurseryReport(AppUser n){
    List<TreeOrder> os=orders.findByNurseryOrderByCreatedAtDesc(n); Map<String,Long> byStatus=new LinkedHashMap<>();
    for(String s:List.of(Const.PENDING,Const.CONFIRMED,Const.DELIVERED,Const.CANCELLED)) byStatus.put(s,os.stream().filter(o->o.getStatus().equals(s)).count());
    List<Tree> ts=trees.findByNurseryOrderByIdDesc(n); Map<String,Object> m=new LinkedHashMap<>();
    m.put("ordersByStatus",byStatus); m.put("revenueDelivered",orders.revenue(n,Const.DELIVERED)); m.put("revenuePendingOrConfirmed",orders.revenue(n,Const.PENDING)+orders.revenue(n,Const.CONFIRMED));
    m.put("unitsInStock",ts.stream().mapToInt(Tree::getQuantity).sum()); m.put("lowStockLines",ts.stream().filter(Tree::isLowStock).count());
    m.put("newRequests",requests.countByNurseryAndStatus(n,Const.REQ_REQUESTED));
    m.put("bestSellers",os.stream().filter(o->!Const.CANCELLED.equals(o.getStatus())).collect(java.util.stream.Collectors.groupingBy(o->o.getTree().getName(),java.util.stream.Collectors.summingInt(TreeOrder::getQuantity))));
    m.put("recentActivity",log.byActor(n.getEmail())); return m;
  }
  public Map<String,Object> userReport(AppUser u){
    Map<String,Object> m=new LinkedHashMap<>();
    m.put("plantationsSubmitted",plants.countByUser(u)); m.put("plantationsVerified",plants.countByUserAndStatus(u,Const.VERIFIED));
    m.put("consumptionLogs",consumption.countByUser(u)); m.put("carbon",carbon.summary(u)); m.put("reportsFiled",reports.findByReporterOrderByCreatedAtDesc(u).size());
    m.put("recentActivity",log.byActor(u.getEmail())); return m;
  }
}
