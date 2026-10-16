package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface PlantationRequestRepo extends JpaRepository<PlantationRequest,Long>{
  List<PlantationRequest> findByRequesterOrderByCreatedAtDesc(AppUser u); List<PlantationRequest> findByNurseryOrderByCreatedAtDesc(AppUser n);
  long countByNurseryAndStatus(AppUser n,String s); List<PlantationRequest> findAllByOrderByCreatedAtDesc();
}
