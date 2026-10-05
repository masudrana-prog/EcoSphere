package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface ActivityLogRepo extends JpaRepository<ActivityLog,Long>{
  List<ActivityLog> findTop100ByOrderByOccurredAtDesc(); List<ActivityLog> findTop50ByActorEmailOrderByOccurredAtDesc(String email);
  List<ActivityLog> findTop100ByEntityTypeOrderByOccurredAtDesc(String type); long countByOccurredAtAfter(LocalDateTime t);
}
