package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface TreeSpeciesRepo extends JpaRepository<TreeSpecies,Long>{
  List<TreeSpecies> findByActiveTrueOrderByNameAsc(); Optional<TreeSpecies> findByNameIgnoreCase(String n);
  List<TreeSpecies> findByNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(String a,String b);
}
