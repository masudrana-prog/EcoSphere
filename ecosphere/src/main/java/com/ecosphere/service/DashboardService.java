package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.util.*;

/** Sustainability dashboard for citizens (ticket 19) and the admin overview (ticket 25). */
@Service @RequiredArgsConstructor
public class DashboardService {
  private final CarbonService carbon; private final ConsumptionService consumptionService; private final PlantationService plantations; private final BadgeService badges;
  private final GreenScoreService greenScore; private final NotificationService notifications; private final UserRepo users; private final TreeRepo trees;
  private final TreeOrderRepo orders; private final ReportRepo reports; private final PlantationRepo plantRepo; private final PlantationRequestRepo requests; private final ActivityLogService log;

  /** Everything the user dashboard shows, in one call. */
  public Map<String,Object> userDashboard(AppUser u){
    List<Plantation> mine=plantations.mine(u); long verified=mine.stream().filter(p->Const.VERIFIED.equals(p.getStatus())).count();
    Map<String,Object> m=new LinkedHashMap<>();
    m.put("ecoPoints",u.getEcoPoints()); m.put("greenScore",u.getGreenScore()); m.put("rank",greenScore.rankOf(u));
    m.put("carbon",carbon.summary(u)); m.put("monthlyCo2",consumptionService.monthlyTrend(u,6));
    m.put("plantations",Map.of("total",mine.size(),"verified",verified,"pending",mine.stream().filter(p->Const.PENDING.equals(p.getStatus())).count(),
      "rejected",mine.stream().filter(p->Const.REJECTED.equals(p.getStatus())).count()));
    m.put("co2SavedKgPerYear",verified*CarbonService.CO2_PER_TREE_PER_YEAR); m.put("badges",badges.earnedNames(u));
    m.put("topStandings",greenScore.leaderboard("points",3)); m.put("unreadNotifications",notifications.unreadCount(u)); return m;
  }

  /** Admin overview cards (ticket 25). */
  public Map<String,Object> adminOverview(){
    Map<String,Object> m=new LinkedHashMap<>(); Map<String,Object> people=new LinkedHashMap<>();
    for(String r:List.of(Const.ROLE_USER,Const.ROLE_NGO,Const.ROLE_NURSERY,Const.ROLE_ADMIN)) people.put(r,users.countByRole(r));
    m.put("usersByRole",people); m.put("totalUsers",users.count());
    m.put("pendingApprovals",users.findByStatus(Const.PENDING).size()); m.put("suspendedAccounts",users.findByStatus(Const.SUSPENDED).size());
    m.put("plantations",plantations.statusCounts());
    m.put("liveStockUnits",trees.findAll().stream().mapToInt(Tree::getQuantity).sum());
    m.put("orders",Map.of("pending",orders.countByStatus(Const.PENDING),"confirmed",orders.countByStatus(Const.CONFIRMED),"delivered",orders.countByStatus(Const.DELIVERED),"cancelled",orders.countByStatus(Const.CANCELLED)));
    m.put("openReports",reports.countByStatus(Const.REPORT_OPEN)); m.put("recentActivity",log.recent().stream().limit(10).toList()); return m;
  }
}
