package com.ecosphere.controller;
import com.ecosphere.entity.*; import com.ecosphere.repository.UserRepo; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.io.IOException; import java.security.Principal; import java.time.LocalDate; import java.util.List;

/** Sprint 2 citizen dashboard: plantations and tree catalogue. Carbon, badges and leaderboard arrive in Sprint 3. */
@Controller @RequiredArgsConstructor
public class UserController {
  private final UserLookup lookup; private final PlantationService plantations; private final TreeService trees; private final UserRepo users;

  @GetMapping("/user") String dash(Model m,Principal p){
    AppUser me=lookup.byEmail(p.getName()); var pl=plantations.mine(me);
    long verified=pl.stream().filter(x->"VERIFIED".equals(x.getStatus())).count();
    var board=users.findByRoleOrderByEcoPointsDesc("USER"); int rank=1; for(AppUser u:board){ if(u.getId().equals(me.getId())) break; rank++; }
    m.addAttribute("plants",pl); m.addAttribute("verified",verified); m.addAttribute("trees",trees.catalogue(null,null));
    m.addAttribute("logs",List.of()); m.addAttribute("badges",List.of()); m.addAttribute("total",0.0); m.addAttribute("saved",verified*225.0);
    m.addAttribute("rank",rank); m.addAttribute("board",board.stream().limit(3).toList()); return "user";
  }
  @PostMapping("/user/consumption") String log(RedirectAttributes ra){ ra.addFlashAttribute("error","Consumption tracking is delivered in Sprint 3."); return "redirect:/user"; }
  @PostMapping("/user/plantation")
  String submit(@RequestParam String treeName,@RequestParam String location,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate plantedOn,@RequestParam(required=false) Double lat,@RequestParam(required=false) Double lng,
      @RequestParam(required=false) Long savedLocationId,@RequestParam(required=false) MultipartFile photo,Principal p,RedirectAttributes ra) throws IOException {
    try{ plantations.submit(lookup.byEmail(p.getName()),treeName,location,plantedOn,lat,lng,savedLocationId,photo); ra.addFlashAttribute("success","Plantation submitted for verification."); }
    catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/user";
  }
}
