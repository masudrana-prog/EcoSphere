package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface TreeRepo extends JpaRepository<Tree,Long>{
  List<Tree> findByNurseryOrderByIdDesc(AppUser n);
  List<Tree> findByListedTrueOrderByNameAsc();
  List<Tree> findBySpecies(TreeSpecies s);
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from Tree t where t.id=:id") Optional<Tree> lockById(@Param("id") Long id);
}
