package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface UserBadgeRepo extends JpaRepository<UserBadge,Long>{
  List<UserBadge> findByUserOrderByAwardedAtDesc(AppUser u); boolean existsByUserAndBadge(AppUser u,Badge b); long countByBadge(Badge b);
  void deleteByBadge(Badge b);
}
