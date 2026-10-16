package com.ecosphere.controller;
import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Controller; import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.io.IOException; import java.security.Principal; import java.util.*;

/** Profile page for every role (tickets 7, 9, 13, 14). */
@Controller @RequestMapping("/profile") @RequiredArgsConstructor
public class ProfileController {
  private final UserLookup lookup; private final ProfileService profiles;
  @GetMapping String view(Model m,Principal p){ m.addAttribute("profile",profiles.get(lookup.byEmail(p.getName()))); return "profile"; }
  @PostMapping String update(@RequestParam Map<String,String> form,Principal p,RedirectAttributes ra){
    Map<String,String> f=new HashMap<>(form); f.remove("_csrf");
    try{ profiles.update(lookup.byEmail(p.getName()),f); ra.addFlashAttribute("success","Profile updated."); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/profile"; }
  @PostMapping("/photo") String photo(@RequestParam MultipartFile photo,Principal p,RedirectAttributes ra) throws IOException {
    try{ profiles.uploadPhoto(lookup.byEmail(p.getName()),photo); ra.addFlashAttribute("success","Photo updated."); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/profile"; }
  @PostMapping("/password") String password(@RequestParam String current,@RequestParam String next,@RequestParam String confirm,Principal p,RedirectAttributes ra){
    try{ profiles.changePassword(lookup.byEmail(p.getName()),current,next,confirm); ra.addFlashAttribute("success","Password changed."); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/profile"; }
}
