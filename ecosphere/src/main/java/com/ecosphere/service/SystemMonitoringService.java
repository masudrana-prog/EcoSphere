package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.lang.management.ManagementFactory; import java.time.*; import java.util.*;

/** System health and platform analytics for admins (tickets 30, 34). */
@Service @RequiredArgsConstructor
public class SystemMonitoringService {
  private final UserRepo users; private final PlantationRepo plants; private final ConsumptionRepo consumption; private final TreeOrderRepo orders;
  private final ActivityLogRepo logs; private final ReportRepo reports; private final CarbonService carbon;

  /** JVM / database health snapshot. */
  public Map<String,Object> health(){
    Runtime rt=Runtime.getRuntime(); Map<String,Object> m=new LinkedHashMap<>();
    m.put("status","UP"); m.put("uptimeMinutes",ManagementFactory.getRuntimeMXBean().getUptime()/60000);
    m.put("usedMemoryMb",(rt.totalMemory()-rt.freeMemory())/1048576); m.put("maxMemoryMb",rt.maxMemory()/1048576); m.put("processors",rt.availableProcessors());
    try{ m.put("database","UP ("+users.count()+" accounts)"); }catch(Exception e){ m.put("database","DOWN: "+e.getMessage()); m.put("status","DEGRADED"); }
    m.put("activityLast24h",logs.countByOccurredAtAfter(LocalDateTime.now().minusHours(24))); m.put("serverTime",LocalDateTime.now().toString()); return m;
  }
  /** Platform analytics: environmental impact, trends and top contributors. */
  public Map<String,Object> analytics(int days){
    int d=Math.max(1,Math.min(90,days)); LocalDate today=LocalDate.now();
    Map<String,Long> submissionsPerDay=new LinkedHashMap<>(), registrationsPerDay=new LinkedHashMap<>();
    for(int i=d-1;i>=0;i--){ submissionsPerDay.put(today.minusDays(i).toString(),0L); registrationsPerDay.put(today.minusDays(i).toString(),0L); }
    for(Plantation p:plants.findAll()) submissionsPerDay.computeIfPresent(p.getSubmittedAt().toLocalDate().toString(),(k,v)->v+1);
    for(AppUser u:users.findAll()) registrationsPerDay.computeIfPresent(u.getCreatedAt().toLocalDate().toString(),(k,v)->v+1);
    long verified=plants.countByStatus(Const.VERIFIED), decided=verified+plants.countByStatus(Const.REJECTED);
    Map<String,Object> m=new LinkedHashMap<>();
    m.put("verifiedTrees",verified); m.put("verificationRatePercent",decided==0?0:Math.round(verified*1000.0/decided)/10.0);
    m.put("co2OffsetKgPerYear",verified*CarbonService.CO2_PER_TREE_PER_YEAR); m.put("co2LoggedKg",Math.round(consumption.totalCo2()*100)/100.0);
    m.put("ordersDelivered",orders.countByStatus(Const.DELIVERED)); m.put("openReports",reports.countByStatus(Const.REPORT_OPEN));
    m.put("submissionsPerDay",submissionsPerDay); m.put("registrationsPerDay",registrationsPerDay);
    m.put("topContributors",users.findByRoleOrderByEcoPointsDesc(Const.ROLE_USER).stream().limit(5).map(u->Map.of("name",u.getName(),"ecoPoints",u.getEcoPoints(),"greenScore",u.getGreenScore())).toList());
    m.put("emissionFactors",carbon.factors()); return m;
  }
  public List<ActivityLog> recentActivity(String entityType){
    return entityType==null||entityType.isBlank()?logs.findTop100ByOrderByOccurredAtDesc():logs.findTop100ByEntityTypeOrderByOccurredAtDesc(entityType);
  }
  /** CSV export of recent activity for audits. */
  public String activityCsv(){
    StringBuilder sb=new StringBuilder("time,actor,role,action,entity,entityId,details\n");
    for(ActivityLog l:logs.findTop100ByOrderByOccurredAtDesc())
      sb.append(l.getOccurredAt()).append(',').append(esc(l.getActorEmail())).append(',').append(esc(l.getActorRole())).append(',').append(esc(l.getAction())).append(',')
        .append(esc(l.getEntityType())).append(',').append(l.getEntityId()==null?"":l.getEntityId()).append(',').append(esc(l.getDetails())).append('\n');
    return sb.toString();
  }
  private static String esc(String s){ return s==null?"":"\""+s.replace("\"","\"\"")+"\""; }
}
