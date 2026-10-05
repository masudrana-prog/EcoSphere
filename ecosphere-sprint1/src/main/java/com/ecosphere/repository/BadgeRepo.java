package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface BadgeRepo extends JpaRepository<Badge,Long>{ List<Badge> findByActiveTrue(); Optional<Badge> findByName(String n); }
