package com.ecosphere.service;
import com.ecosphere.dto.LeaderboardEntry; import com.ecosphere.entity.AppUser;
import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import java.util.*;

/** Green Score (0-1000) and the leaderboard (ticket 18). */
@Service @RequiredArgsConstructor
public class GreenScoreService {
  public static final int MAX_SCORE=1000;
  private final UserRepo users; private final PlantationRepo plants;

  public AppUser add(AppUser u,int delta){ u.setGreenScore(Math.max(0,Math.min(MAX_SCORE,u.getGreenScore()+delta))); return users.save(u); }

  /** Leaderboard sorted by EcoPoints (default) or GreenScore; limit<=0 means everyone. */
  public List<LeaderboardEntry> leaderboard(String sortBy,int limit){
    List<AppUser> list="score".equalsIgnoreCase(sortBy)?users.findByRoleOrderByGreenScoreDesc("USER"):users.findByRoleOrderByEcoPointsDesc("USER");
    List<LeaderboardEntry> out=new ArrayList<>(); int rank=1;
    for(AppUser u:list){ if(limit>0&&rank>limit) break;
      out.add(new LeaderboardEntry(rank++,u.getId(),u.getName(),u.getLocation(),u.getEcoPoints(),u.getGreenScore(),plants.countByUserAndStatus(u,"VERIFIED"))); }
    return out;
  }
  public int rankOf(AppUser me){
    int rank=1; for(AppUser u:users.findByRoleOrderByEcoPointsDesc("USER")){ if(u.getId().equals(me.getId())) return rank; rank++; }
    return rank;
  }
}
