package com.ecosphere.controller;
import com.ecosphere.entity.AppUser; import com.ecosphere.repository.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.security.Principal;

/** Admin dashboard page (tickets 25, 26, 34). */
@Controller @RequestMapping("/admin") @RequiredArgsConstructor
public class AdminController {
  private final UserLookup lookup; private final UserRepo users; private final PlantationRepo plants; private final TreeRepo trees; private final ConfigService config; private final AdminUserService adminUsers;
  @GetMapping String dash(Model m){ m.addAttribute("all",users.findAll()); m.addAttribute("configs",config.all());
    m.addAttribute("nUsers",users.countByRole("USER")); m.addAttribute("nNgo",users.countByRole("NGO")); m.addAttribute("nNur",users.countByRole("NURSERY"));
    m.addAttribute("subs",plants.count()); m.addAttribute("pending",plants.countByStatus("PENDING")); m.addAttribute("stock",trees.findAll().stream().mapToInt(t->t.getQuantity()).sum()); return "admin"; }
  @PostMapping("/user/{id}/status") String status(@PathVariable Long id,@RequestParam String status,Principal p,RedirectAttributes ra){
    try{ adminUsers.setStatus(lookup.byEmail(p.getName()),id,status,null); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/admin"; }
  @PostMapping("/config") String config(@RequestParam String key,@RequestParam String value,RedirectAttributes ra){
    try{ config.save(key,value); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/admin"; }
}
