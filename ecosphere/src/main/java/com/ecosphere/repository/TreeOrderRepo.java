package com.ecosphere.repository;
import com.ecosphere.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.time.LocalDateTime; import java.util.*;

public interface TreeOrderRepo extends JpaRepository<TreeOrder,Long>{
  List<TreeOrder> findByBuyerOrderByCreatedAtDesc(AppUser u); List<TreeOrder> findByNurseryOrderByCreatedAtDesc(AppUser n);
  long countByNurseryAndStatus(AppUser n,String s); boolean existsByTree(Tree t); boolean existsByTrxId(String trxId);
  long countByStatus(String s);
  @Query("select coalesce(sum(o.total),0) from TreeOrder o where o.nursery=:n and o.status=:s") double revenue(@Param("n") AppUser n,@Param("s") String s);
}
