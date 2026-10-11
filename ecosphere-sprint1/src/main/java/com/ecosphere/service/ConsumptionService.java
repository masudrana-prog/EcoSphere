package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.time.*; import java.util.*;

/** Consumption logging and history (ticket 16). */
@Service @RequiredArgsConstructor
public class ConsumptionService {
  private final ConsumptionRepo repo; private final CarbonService carbon; private final BadgeService badges; private final GreenScoreService greenScore;

  public Consumption log(AppUser u,String category,double amount,String note){
    String cat=category==null?"":category.trim().toUpperCase();
    double co2=carbon.calculate(cat,amount);
    Consumption c=new Consumption(); c.setUser(u); c.setCategory(cat); c.setAmount(amount); c.setCo2(co2); c.setNote(note); c=repo.save(c);
    greenScore.add(u,1); badges.evaluate(u); return c;
  }
  public List<Consumption> history(AppUser u,Integer days){
    return days==null||days<=0?repo.findByUserOrderByLoggedAtDesc(u):repo.findByUserAndLoggedAtAfterOrderByLoggedAtDesc(u,LocalDateTime.now().minusDays(days));
  }
  public Consumption update(AppUser u,Long id,double amount,String note){
    Consumption c=owned(u,id); c.setAmount(amount); c.setCo2(carbon.calculate(c.getCategory(),amount)); c.setNote(note); return repo.save(c);
  }
  public void delete(AppUser u,Long id){ repo.delete(owned(u,id)); }
  private Consumption owned(AppUser u,Long id){
    Consumption c=repo.findById(id).orElseThrow(()->new NoSuchElementException("Record not found."));
    if(!c.getUser().getId().equals(u.getId())) throw new SecurityException("Not your record."); return c;
  }
  /** CO2 per calendar month for the last n months, oldest first. */
  public Map<String,Double> monthlyTrend(AppUser u,int months){
    Map<String,Double> m=new LinkedHashMap<>(); YearMonth now=YearMonth.now();
    for(int i=months-1;i>=0;i--) m.put(now.minusMonths(i).toString(),0.0);
    for(Consumption c:repo.findByUserOrderByLoggedAtDesc(u)) m.computeIfPresent(YearMonth.from(c.getLoggedAt()).toString(),(k,v)->Math.round((v+c.getCo2())*100)/100.0);
    return m;
  }
}
