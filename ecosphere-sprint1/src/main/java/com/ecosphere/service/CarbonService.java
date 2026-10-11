package com.ecosphere.service;
import com.ecosphere.dto.CarbonSummary; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.util.*;

/** Carbon footprint calculation from consumption data (ticket 15). Emission factors can be overridden in System Config as EF_<CATEGORY>. */
@Service @RequiredArgsConstructor
public class CarbonService {
  public static final double CO2_PER_TREE_PER_YEAR=22.5; // kg, EPA-style average
  private static final Map<String,Double> DEFAULTS=new LinkedHashMap<>();
  static { DEFAULTS.put("ELECTRICITY",0.38); DEFAULTS.put("TRANSPORT",0.19); DEFAULTS.put("WATER",0.0011); DEFAULTS.put("WASTE",0.5); DEFAULTS.put("LPG",2.98); }
  private final ConfigService config; private final ConsumptionRepo consumption; private final PlantationRepo plants;

  public static Set<String> categories(){ return DEFAULTS.keySet(); }
  public static String unit(String c){ return switch(c){ case "ELECTRICITY"->"kWh"; case "TRANSPORT"->"km"; case "WATER"->"L"; case "WASTE"->"kg"; case "LPG"->"kg"; default->""; }; }
  public double factor(String category){ return config.getDouble("EF_"+category,DEFAULTS.get(category)); }

  public double calculate(String category,double amount){
    if(category==null||!DEFAULTS.containsKey(category.toUpperCase())) throw new IllegalArgumentException("Unknown category. Use one of "+DEFAULTS.keySet());
    if(amount<=0||Double.isNaN(amount)||Double.isInfinite(amount)) throw new IllegalArgumentException("Amount must be greater than zero.");
    return Math.round(amount*factor(category.toUpperCase())*100)/100.0;
  }
  public Map<String,Double> factors(){ Map<String,Double> m=new LinkedHashMap<>(); DEFAULTS.keySet().forEach(k->m.put(k,factor(k))); return m; }

  public CarbonSummary summary(AppUser u){
    Map<String,Double> by=new LinkedHashMap<>(); DEFAULTS.keySet().forEach(k->by.put(k,0.0)); double total=0;
    for(Consumption c:consumption.findByUserOrderByLoggedAtDesc(u)){ by.merge(c.getCategory(),c.getCo2(),Double::sum); total+=c.getCo2(); }
    by.replaceAll((k,v)->Math.round(v*100)/100.0); total=Math.round(total*100)/100.0;
    double offset=plants.countByUserAndStatus(u,"VERIFIED")*CO2_PER_TREE_PER_YEAR;
    return new CarbonSummary(total,by,Math.round(total/CO2_PER_TREE_PER_YEAR*10)/10.0,offset,Math.round((total-offset)*100)/100.0);
  }
}
