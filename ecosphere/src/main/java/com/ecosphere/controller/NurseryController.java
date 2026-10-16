package com.ecosphere.controller;
import com.ecosphere.entity.*; import com.ecosphere.repository.TreeOrderRepo; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.security.Principal;

/** Nursery dashboard page (tickets 20, 21). */
@Controller @RequestMapping("/nursery") @RequiredArgsConstructor
public class NurseryController {
  private final UserLookup lookup; private final TreeService trees; private final OrderService orders; private final TreeOrderRepo orderRepo;
  @GetMapping String dash(Model m,Principal p){ AppUser me=lookup.byEmail(p.getName()); var list=trees.mine(me); m.addAttribute("trees",list);
    m.addAttribute("units",list.stream().mapToInt(Tree::getQuantity).sum()); m.addAttribute("orders",orders.forNursery(me)); m.addAttribute("pendingOrders",orderRepo.countByNurseryAndStatus(me,"PENDING")); return "nursery"; }
  @PostMapping("/tree") String add(@RequestParam String name,@RequestParam(defaultValue="") String scientificName,@RequestParam String category,@RequestParam double price,@RequestParam int quantity,Principal p,RedirectAttributes ra){
    try{ trees.add(lookup.byEmail(p.getName()),name,scientificName,category,price,quantity,null); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/nursery"; }
  @PostMapping("/tree/{id}/stock") String stock(@PathVariable Long id,@RequestParam int quantity,Principal p,RedirectAttributes ra){
    try{ trees.setStock(lookup.byEmail(p.getName()),id,quantity); }catch(SecurityException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/nursery"; }
  @PostMapping("/tree/{id}/delete") String del(@PathVariable Long id,Principal p,RedirectAttributes ra){
    try{ trees.remove(lookup.byEmail(p.getName()),id); }catch(SecurityException e){ ra.addFlashAttribute("error",e.getMessage()); } return "redirect:/nursery"; }
}
