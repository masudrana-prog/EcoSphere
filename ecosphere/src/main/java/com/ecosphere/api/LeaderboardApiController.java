package com.ecosphere.api;
import com.ecosphere.dto.LeaderboardEntry; import com.ecosphere.service.GreenScoreService; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.util.List;

/** Green score leaderboard (ticket 18). Any logged-in user may view it. */
@RestController @RequestMapping("/api/leaderboard") @RequiredArgsConstructor
public class LeaderboardApiController {
  private final GreenScoreService greenScore;
  @GetMapping List<LeaderboardEntry> leaderboard(@RequestParam(defaultValue="points") String sort,@RequestParam(defaultValue="10") int limit){ return greenScore.leaderboard(sort,limit); }
}
