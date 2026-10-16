package com.ecosphere.api;
import com.ecosphere.dto.CarbonSummary; import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

/** Carbon footprint calculation (15) and consumption tracking (16). */
@RestController @RequestMapping("/api/user") @RequiredArgsConstructor
public class UserCarbonApiController {
  private final UserLookup lookup; private final CarbonService carbon; private final ConsumptionService consumption;
  private AppUser me(Principal p){ return lookup.byEmail(p.getName()); }
  @GetMapping("/carbon/summary") CarbonSummary summary(Principal p){ return carbon.summary(me(p)); }
  @GetMapping("/carbon/factors") Map<String,Double> factors(){ return carbon.factors(); }
  @GetMapping("/carbon/calculate") Map<String,Object> calculate(@RequestParam String category,@RequestParam double amount){
    String c=category.toUpperCase(); return Map.of("category",c,"amount",amount,"unit",CarbonService.unit(c),"co2Kg",carbon.calculate(c,amount)); }
  public record ConsumptionBody(String category,double amount,String note) {}
  @PostMapping("/consumption") ResponseEntity<Consumption> log(@RequestBody ConsumptionBody b,Principal p){ return ResponseEntity.status(HttpStatus.CREATED).body(consumption.log(me(p),b.category(),b.amount(),b.note())); }
  @GetMapping("/consumption") List<Consumption> history(@RequestParam(required=false) Integer days,Principal p){ return consumption.history(me(p),days); }
  @PutMapping("/consumption/{id}") Consumption update(@PathVariable Long id,@RequestBody ConsumptionBody b,Principal p){ return consumption.update(me(p),id,b.amount(),b.note()); }
  @DeleteMapping("/consumption/{id}") Map<String,String> delete(@PathVariable Long id,Principal p){ consumption.delete(me(p),id); return Map.of("message","Record deleted."); }
  @GetMapping("/consumption/trend") Map<String,Double> trend(@RequestParam(defaultValue="6") int months,Principal p){ return consumption.monthlyTrend(me(p),Math.max(1,Math.min(24,months))); }
}
