package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface NotificationRepo extends JpaRepository<Notification,Long>{
  List<Notification> findByRecipientOrderByCreatedAtDesc(AppUser u); long countByRecipientAndSeenFalse(AppUser u);
  List<Notification> findByRecipientAndSeenFalse(AppUser u); List<Notification> findAllByOrderByCreatedAtDesc();
  List<Notification> findByTypeOrderByCreatedAtDesc(String type);
}
