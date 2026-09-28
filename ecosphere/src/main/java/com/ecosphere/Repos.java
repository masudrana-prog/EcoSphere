package com.ecosphere;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
interface UserRepo extends JpaRepository<AppUser,Long>{ Optional<AppUser> findByEmail(String e); List<AppUser> findByRoleOrderByEcoPointsDesc(String r); long countByRole(String r); }
interface PlantationRepo extends JpaRepository<Plantation,Long>{ List<Plantation> findByUserOrderBySubmittedAtDesc(AppUser u); List<Plantation> findByStatusOrderBySubmittedAtDesc(String s); List<Plantation> findByStatusNotOrderBySubmittedAtDesc(String s); long countByStatus(String s); }
