package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** EcoPoints ledger operations and reward redemption (tickets 12, 31). */
@Service @RequiredArgsConstructor
public class EcoPointsService {
  private final UserRepo users; private final RewardRepo rewards; private final RewardRedemptionRepo redemptions;
  private final NotificationService notifications; private final ActivityLogService log;

  @Transactional public AppUser award(AppUser u,int points){ if(points<=0) throw new IllegalArgumentException("Points must be positive."); u.setEcoPoints(u.getEcoPoints()+points); return users.save(u); }

  // ---- user side
  public List<Reward> catalog(){ return rewards.findByActiveTrueOrderByPointsCostAsc(); }
  public List<RewardRedemption> myRedemptions(AppUser u){ return redemptions.findByUserOrderByRedeemedAtDesc(u); }

  @Transactional
  public RewardRedemption redeem(AppUser u,Long rewardId){
    Reward r=rewards.lockById(rewardId).orElseThrow(()->new NoSuchElementException("Reward not found."));
    AppUser fresh=users.findById(u.getId()).orElseThrow();
    if(!r.isActive()) throw new IllegalArgumentException("This reward is no longer available.");
    if(r.getStock()<1) throw new IllegalArgumentException("This reward is out of stock.");
    if(fresh.getEcoPoints()<r.getPointsCost()) throw new IllegalArgumentException("Not enough EcoPoints: you need "+r.getPointsCost()+" but have "+fresh.getEcoPoints()+".");
    fresh.setEcoPoints(fresh.getEcoPoints()-r.getPointsCost()); users.save(fresh);
    r.setStock(r.getStock()-1); rewards.save(r);
    RewardRedemption x=new RewardRedemption(); x.setUser(fresh); x.setReward(r); x.setPointsSpent(r.getPointsCost()); x=redemptions.save(x);
    notifications.notify(fresh,"Reward redeemed","You redeemed \""+r.getName()+"\" for "+r.getPointsCost()+" EcoPoints.","REWARD",null);
    log.log(fresh,"REWARD_REDEEMED","Reward",r.getId(),r.getName()); return x;
  }

  // ---- admin side (ticket 31)
  public List<Reward> allRewards(){ return rewards.findAll(); }
  public List<RewardRedemption> allRedemptions(){ return redemptions.findAllByOrderByRedeemedAtDesc(); }
  @Transactional
  public Reward saveReward(Long id,String name,String description,int pointsCost,int stock,boolean active,AppUser admin){
    if(name==null||name.isBlank()) throw new IllegalArgumentException("Reward name is required.");
    if(pointsCost<1) throw new IllegalArgumentException("Points cost must be at least 1.");
    if(stock<0) throw new IllegalArgumentException("Stock cannot be negative.");
    Reward r=id==null?new Reward():rewards.findById(id).orElseThrow(()->new NoSuchElementException("Reward not found."));
    r.setName(name.trim()); r.setDescription(description); r.setPointsCost(pointsCost); r.setStock(stock); r.setActive(active);
    r=rewards.save(r); log.log(admin,id==null?"REWARD_CREATED":"REWARD_UPDATED","Reward",r.getId(),r.getName()); return r;
  }
  @Transactional
  public void deleteReward(Long id,AppUser admin){
    Reward r=rewards.findById(id).orElseThrow(()->new NoSuchElementException("Reward not found."));
    if(redemptions.existsByReward(r)){ r.setActive(false); rewards.save(r); } else rewards.delete(r); // keep history if already redeemed
    log.log(admin,"REWARD_REMOVED","Reward",id,r.getName());
  }
}
