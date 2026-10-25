package com.ecosphere.controller;
import com.ecosphere.service.OrderService; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.security.Principal;

@Controller @RequestMapping("/nursery/orders") @RequiredArgsConstructor
public class NurseryOrderController {
  private final OrderService svc;
  @PostMapping("/{id}/status") String status(@PathVariable Long id,@RequestParam String status,Principal p,RedirectAttributes ra){
    try{ svc.changeStatus(id,p.getName(),true,status); ra.addFlashAttribute("success","Order #"+id+" marked "+status+"."); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); }
    return "redirect:/nursery"; }
}
