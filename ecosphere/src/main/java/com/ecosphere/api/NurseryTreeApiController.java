package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Nursery stock (ticket 20), availability (ticket 21) and incoming tree orders. */
@RestController @RequestMapping("/api/nursery") @RequiredArgsConstructor
public class NurseryTreeApiController {
  private final UserLookup lookup; private final TreeService trees; private final NurseryAvailabilityService availability; private final OrderService orders;
  private AppUser me(Principal p){ return lookup.byEmail(p.getName()); }

  public record TreeBody(String name,String scientificName,String category,Double price,Integer quantity,Long speciesId) {}
  @GetMapping("/trees") List<Tree> myTrees(Principal p){ return trees.mine(me(p)); }
  @PostMapping("/trees") ResponseEntity<Tree> add(@RequestBody TreeBody b,Principal p){
    return ResponseEntity.status(HttpStatus.CREATED).body(trees.add(me(p),b.name(),b.scientificName(),b.category(),b.price()==null?0:b.price(),b.quantity()==null?0:b.quantity(),b.speciesId())); }
  @PutMapping("/trees/{id}") Tree update(@PathVariable Long id,@RequestBody TreeBody b,Principal p){ return trees.update(me(p),id,b.name(),b.scientificName(),b.category(),b.price()); }
  @DeleteMapping("/trees/{id}") Map<String,String> remove(@PathVariable Long id,Principal p){ trees.remove(me(p),id); return Map.of("message","Tree removed."); }

  public record QuantityBody(int quantity) {}
  @PutMapping("/trees/{id}/stock") Tree stock(@PathVariable Long id,@RequestBody QuantityBody b,Principal p){ return trees.setStock(me(p),id,b.quantity()); }
  @PostMapping("/trees/{id}/restock") Tree restock(@PathVariable Long id,@RequestBody QuantityBody b,Principal p){ return availability.restock(me(p),id,b.quantity()); }
  public record ListedBody(boolean listed) {}
  @PutMapping("/trees/{id}/listed") Tree listed(@PathVariable Long id,@RequestBody ListedBody b,Principal p){ return availability.setListed(me(p),id,b.listed()); }
  public record ThresholdBody(int threshold) {}
  @PutMapping("/trees/{id}/low-stock-threshold") Tree threshold(@PathVariable Long id,@RequestBody ThresholdBody b,Principal p){ return availability.setThreshold(me(p),id,b.threshold()); }
  @GetMapping("/availability") Map<String,Object> availability(Principal p){ return availability.summary(me(p)); }
  @PostMapping("/low-stock-alerts") Map<String,Integer> alerts(Principal p){ return Map.of("sent",availability.sendLowStockAlerts(me(p))); }

  @GetMapping("/orders") List<TreeOrder> orders(Principal p){ return orders.forNursery(me(p)); }
  public record StatusBody(String status) {}
  @PostMapping("/orders/{id}/status") Map<String,String> orderStatus(@PathVariable Long id,@RequestBody StatusBody b,Principal p){ orders.changeStatus(id,p.getName(),true,b.status()); return Map.of("message","Order #"+id+" marked "+b.status()+"."); }
}
