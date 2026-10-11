package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** Badge evaluation for users and badge administration (tickets 12, 31). */
@Service @RequiredArgsConstructor
public class BadgeService {
  private final BadgeRepo badges; private final UserBadgeRepo userBadges; private final UserRepo users; private final PlantationRepo plants;
  private final ConsumptionRepo consumption; private final NotificationService notifications; private final GreenScoreService greenScore; private final ActivityLogService log;

  public List<UserBadge> earned(AppUser u){ return userBadges.findByUserOrderByAwardedAtDesc(u); }
  public List<String> earnedNames(AppUser u){ return earned(u).stream().map(b->b.getBadge().getName()).toList(); }

  /** Checks every active badge and awards the ones newly earned. Safe to call repeatedly. */
  @Transactional
  public List<Badge> evaluate(AppUser u){
    List<Badge> fresh=new ArrayList<>();
    for(Badge b:badges.findByActiveTrue()){
      if(userBadges.existsByUserAndBadge(u,b)||!qualifies(u,b)) continue;
      UserBadge ub=new UserBadge(); ub.setUser(u); ub.setBadge(b); userBadges.save(ub); fresh.add(b);
      if(b.getBonusPoints()>0){ u.setEcoPoints(u.getEcoPoints()+b.getBonusPoints()); }
      greenScore.add(u,5); users.save(u);
      notifications.notify(u,"New badge: "+b.getName(),b.getDescription()+(b.getBonusPoints()>0?" (+"+b.getBonusPoints()+" bonus EcoPoints)":""),"REWARD",null);
    }
    return fresh;
  }
  private boolean qualifies(AppUser u,Badge b){
    long have=switch(b.getCriteriaType()==null?"":b.getCriteriaType()){
      case Const.CRIT_VERIFIED_PLANTATIONS->plants.countByUserAndStatus(u,Const.VERIFIED);
      case Const.CRIT_CONSUMPTION_LOGS->consumption.countByUser(u);
      case Const.CRIT_ECO_POINTS->u.getEcoPoints();
      case Const.CRIT_GREEN_SCORE->u.getGreenScore();
      case Const.CRIT_FIRST_SUBMISSION->plants.countByUser(u);
      default->0; };
    return have>=Math.max(1,b.getThreshold());
  }

  // ---- admin side (tickets 12, 31)
  public List<Badge> all(){ return badges.findAll(); }
  @Transactional
  public Badge save(Long id,String name,String description,String icon,String criteriaType,int threshold,int bonusPoints,boolean active,AppUser admin){
    if(name==null||name.isBlank()) throw new IllegalArgumentException("Badge name is required.");
    if(!Set.of(Const.CRIT_VERIFIED_PLANTATIONS,Const.CRIT_CONSUMPTION_LOGS,Const.CRIT_ECO_POINTS,Const.CRIT_GREEN_SCORE,Const.CRIT_FIRST_SUBMISSION).contains(criteriaType))
      throw new IllegalArgumentException("Unknown criteria type.");
    if(threshold<1) throw new IllegalArgumentException("Threshold must be at least 1.");
    if(bonusPoints<0) throw new IllegalArgumentException("Bonus points cannot be negative.");
    Badge b=id==null?new Badge():badges.findById(id).orElseThrow(()->new NoSuchElementException("Badge not found."));
    badges.findByName(name.trim()).filter(x->!x.getId().equals(b.getId())).ifPresent(x->{ throw new IllegalArgumentException("A badge with this name already exists."); });
    b.setName(name.trim()); b.setDescription(description); b.setIcon(icon==null||icon.isBlank()?"🏅":icon); b.setCriteriaType(criteriaType);
    b.setThreshold(threshold); b.setBonusPoints(bonusPoints); b.setActive(active);
    Badge saved=badges.save(b); log.log(admin,id==null?"BADGE_CREATED":"BADGE_UPDATED","Badge",saved.getId(),saved.getName()); return saved;
  }
  @Transactional
  public void delete(Long id,AppUser admin){
    Badge b=badges.findById(id).orElseThrow(()->new NoSuchElementException("Badge not found."));
    userBadges.deleteByBadge(b); badges.delete(b); log.log(admin,"BADGE_DELETED","Badge",id,b.getName());
  }
  public long holders(Badge b){ return userBadges.countByBadge(b); }
}
