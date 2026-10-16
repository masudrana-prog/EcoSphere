package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface PlantationRepo extends JpaRepository<Plantation,Long>{
  List<Plantation> findByUserOrderBySubmittedAtDesc(AppUser u);
  List<Plantation> findByStatusOrderBySubmittedAtDesc(String s);
  List<Plantation> findByStatusNotOrderBySubmittedAtDesc(String s);
  List<Plantation> findAllByOrderBySubmittedAtDesc();
  List<Plantation> findByVerifiedByOrderByVerifiedAtDesc(AppUser ngo);
  List<Plantation> findBySavedLocation(PlantationLocation l);
  long countByStatus(String s); long countByUser(AppUser u); long countByUserAndStatus(AppUser u,String s);
  long countByVerifiedBy(AppUser ngo); long countByVerifiedByAndStatus(AppUser ngo,String s);
  long countBySubmittedAtAfter(LocalDateTime t);
}
