package com.ecosphere.controller;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.io.IOException; import java.security.Principal; import java.time.LocalDate;

/** Citizen dashboard page (tickets 5, 15, 16, 18, 19). */
@Controller @RequiredArgsConstructor
public class UserController {
  private final UserLookup lookup; private final ConsumptionService consumption; private final PlantationService plantations; private final TreeService trees;
  private final BadgeService badges; private final GreenScoreService greenScore; private final CarbonService carbon;

  @GetMapping("/user") String dash(Model m,Principal p){
    AppUser me=lookup.byEmail(p.getName()); var logs=consumption.history(me,null); var pl=plantations.mine(me);
    long verified=pl.stream().filter(x->"VERIFIED".equals(x.getStatus())).count();
    m.addAttribute("logs",logs); m.addAttribute("plants",pl); m.addAttribute("verified",verified); m.addAttribute("badges",badges.earnedNames(me));
    m.addAttribute("total",carbon.summary(me).totalCo2Kg()); m.addAttribute("saved",verified*CarbonService.CO2_PER_TREE_PER_YEAR*10);
    m.addAttribute("rank",greenScore.rankOf(me)); m.addAttribute("board",greenScore.leaderboard("points",3).stream().map(e->lookup.byId(e.userId())).toList()); m.addAttribute("trees",trees.catalogue(null,null)); return "user";
  }
  @PostMapping("/user/consumption") String log(@RequestParam String category,@RequestParam double amount,Principal p,RedirectAttributes ra){
    try{ consumption.log(lookup.byEmail(p.getName()),category,amount,null); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/user"; }
  @PostMapping("/user/plantation")
  String submit(@RequestParam String treeName,@RequestParam String location,@RequestParam @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate plantedOn,@RequestParam(required=false) Double lat,@RequestParam(required=false) Double lng,
      @RequestParam(required=false) Long savedLocationId,@RequestParam(required=false) MultipartFile photo,Principal p,RedirectAttributes ra) throws IOException {
    try{ plantations.submit(lookup.byEmail(p.getName()),treeName,location,plantedOn,lat,lng,savedLocationId,photo); ra.addFlashAttribute("success","Plantation submitted for verification."); }
    catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/user"; }
  @GetMapping("/leaderboard") String board(Model m,@RequestParam(defaultValue="points") String sort){
    m.addAttribute("board",greenScore.leaderboard(sort,0).stream().map(e->lookup.byId(e.userId())).toList()); return "leaderboard"; }
}
