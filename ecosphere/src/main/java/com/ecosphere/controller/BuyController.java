package com.ecosphere.controller;
import com.ecosphere.entity.TreeOrder; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.stereotype.Controller; import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.mvc.support.RedirectAttributes; import java.security.Principal;

@Controller @RequestMapping("/user") @RequiredArgsConstructor
public class BuyController {
  private final UserLookup lookup; private final TreeService trees; private final OrderService svc;

  @GetMapping("/buy") String buy(@RequestParam(required=false) String q,@RequestParam(required=false) String category,Model m){
    m.addAttribute("trees",trees.catalogue(q,category)); m.addAttribute("categories",trees.categories()); m.addAttribute("q",q); m.addAttribute("category",category); return "buy"; }
  @PostMapping("/buy/{id}")
  String place(@PathVariable Long id,@RequestParam int quantity,@RequestParam String address,@RequestParam String phone,@RequestParam String payment,@RequestParam(required=false) String bkashSender,@RequestParam(required=false) String trxId,Principal p,RedirectAttributes ra){
    try{ TreeOrder o=svc.place(lookup.byEmail(p.getName()),id,quantity,address,phone,payment,bkashSender,trxId);
      ra.addFlashAttribute("success","Order #"+o.getId()+" placed: "+o.getQuantity()+" × "+o.getTree().getName()+" (৳"+o.getTotal()+"). "+("BKASH".equals(payment)?"The nursery will verify your bKash payment and confirm.":"Pay cash on delivery."));
      return "redirect:/user/orders";
    }catch(IllegalArgumentException|DataIntegrityViolationException e){ ra.addFlashAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"This bKash transaction ID has already been used."); return "redirect:/user/buy"; }
  }
  @GetMapping("/orders") String myOrders(Model m,Principal p){ m.addAttribute("orders",svc.mine(lookup.byEmail(p.getName()))); return "orders"; }
  @PostMapping("/orders/{id}/cancel") String cancel(@PathVariable Long id,Principal p,RedirectAttributes ra){
    try{ svc.changeStatus(id,p.getName(),false,"CANCELLED"); ra.addFlashAttribute("success","Order cancelled."); }catch(IllegalArgumentException e){ ra.addFlashAttribute("error",e.getMessage()); }
    return "redirect:/user/orders"; }
}
