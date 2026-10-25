package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Buying trees from nurseries (COD / bKash). */
@RestController @RequestMapping("/api/user/orders") @RequiredArgsConstructor
public class UserOrderApiController {
  private final UserLookup lookup; private final OrderService orders;
  public record OrderBody(Long treeId,int quantity,String address,String phone,String payment,String bkashSender,String trxId) {}
  @PostMapping ResponseEntity<TreeOrder> order(@RequestBody OrderBody b,Principal p){
    return ResponseEntity.status(HttpStatus.CREATED).body(orders.place(lookup.byEmail(p.getName()),b.treeId(),b.quantity(),b.address(),b.phone(),b.payment(),b.bkashSender(),b.trxId())); }
  @GetMapping List<TreeOrder> mine(Principal p){ return orders.mine(lookup.byEmail(p.getName())); }
  @PostMapping("/{id}/cancel") Map<String,String> cancel(@PathVariable Long id,Principal p){ orders.changeStatus(id,p.getName(),false,"CANCELLED"); return Map.of("message","Order cancelled."); }
}
