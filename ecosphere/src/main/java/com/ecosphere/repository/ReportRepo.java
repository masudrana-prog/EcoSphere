package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface ReportRepo extends JpaRepository<Report,Long>{
  List<Report> findByReporterOrderByCreatedAtDesc(AppUser u); List<Report> findByAgainstOrderByCreatedAtDesc(AppUser u);
  List<Report> findByStatusOrderByCreatedAtDesc(String s); List<Report> findAllByOrderByCreatedAtDesc(); long countByStatus(String s);
}
