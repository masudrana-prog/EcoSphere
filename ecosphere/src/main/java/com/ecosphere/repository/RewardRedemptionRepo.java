package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface RewardRedemptionRepo extends JpaRepository<RewardRedemption,Long>{
  List<RewardRedemption> findByUserOrderByRedeemedAtDesc(AppUser u); List<RewardRedemption> findAllByOrderByRedeemedAtDesc();
  boolean existsByReward(Reward r);
}
