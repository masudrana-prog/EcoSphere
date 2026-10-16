package com.ecosphere.controller;
import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Controller; import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.security.Principal;

/** NGO verification hub (ticket 11). */
@Controller @RequestMapping("/ngo") @RequiredArgsConstructor
public class NgoController {
  private final UserLookup lookup; private final PlantationService plantations;
  @GetMapping String dash(Model m){ var pending=plantations.pendingQueue(); var reviewed=plantations.reviewed(); m.addAttribute("pending",pending); m.addAttribute("reviewed",reviewed);
    m.addAttribute("verifiedCount",plantations.statusCounts().get("VERIFIED")); return "ngo"; }
  @PostMapping("/{id}/approve") String approve(@PathVariable Long id,Principal p,RedirectAttributes ra){
    try{ plantations.approve(id,lookup.byEmail(p.getName())); ra.addFlashAttribute("success","Plantation verified."); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/ngo"; }
  @PostMapping("/{id}/reject") String reject(@PathVariable Long id,@RequestParam String reason,Principal p,RedirectAttributes ra){
    try{ plantations.reject(id,lookup.byEmail(p.getName()),reason); ra.addFlashAttribute("success","Plantation rejected."); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/ngo"; }
}
