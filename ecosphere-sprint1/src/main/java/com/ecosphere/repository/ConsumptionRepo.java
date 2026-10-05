package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface ConsumptionRepo extends JpaRepository<Consumption,Long>{
  List<Consumption> findByUserOrderByLoggedAtDesc(AppUser u);
  List<Consumption> findByUserAndLoggedAtAfterOrderByLoggedAtDesc(AppUser u,LocalDateTime from);
  long countByUser(AppUser u);
  @Query("select coalesce(sum(c.co2),0) from Consumption c") double totalCo2();
}
