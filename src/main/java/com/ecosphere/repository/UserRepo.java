package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface UserRepo extends JpaRepository<AppUser,Long>{
  Optional<AppUser> findByEmail(String e);
  List<AppUser> findByRoleOrderByEcoPointsDesc(String r);
  List<AppUser> findByRoleOrderByGreenScoreDesc(String r);
  List<AppUser> findByRoleOrderByCreatedAtDesc(String r);
  List<AppUser> findByStatus(String s);
  long countByRole(String r); long countByRoleAndStatus(String r,String s);
  List<AppUser> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String n,String e);
}
