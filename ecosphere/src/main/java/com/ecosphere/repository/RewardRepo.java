package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface RewardRepo extends JpaRepository<Reward,Long>{
  List<Reward> findByActiveTrueOrderByPointsCostAsc();
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Reward r where r.id=:id") Optional<Reward> lockById(@Param("id") Long id);
}
