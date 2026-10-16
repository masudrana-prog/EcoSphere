package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface PlantationLocationRepo extends JpaRepository<PlantationLocation,Long>{
  List<PlantationLocation> findByOwnerAndActiveTrueOrderByNameAsc(AppUser o);
}
