package com.ecosphere.config;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Component; import java.time.*;

/** Demo data: accounts, catalogue, species, badges, rewards and system settings (runs once on an empty DB). */
@Component @RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
  private final UserRepo users; private final ConsumptionRepo cons; private final PlantationRepo plants; private final TreeRepo trees; private final ConfigRepo cfg;
  private final TreeSpeciesRepo species; private final BadgeRepo badges; private final RewardRepo rewards; private final PlantationLocationRepo locations;
  private final PasswordEncoder pe; private final UserBadgeRepo userBadges;

  static final java.util.Map<String,Double> FACTOR=java.util.Map.of("ELECTRICITY",0.38,"TRANSPORT",0.19,"WATER",0.0011,"WASTE",0.5);
  AppUser mk(String n,String e,String role,String loc,int pts,int gs){ AppUser u=new AppUser(); u.setName(n);u.setEmail(e);u.setRole(role);u.setLocation(loc);u.setPhone("+8801700000000");
    u.setStatus(Const.ACTIVE);u.setEcoPoints(pts);u.setGreenScore(gs);u.setPasswordHash(pe.encode("password123"));
    if(Const.ROLE_NGO.equals(role)||Const.ROLE_NURSERY.equals(role)) u.setOrganizationName(n);
    return users.save(u); }
  TreeSpecies sp(String name,String sci,String cat,String desc,String care,double co2,int height,int years,String growth){
    TreeSpecies s=new TreeSpecies(); s.setName(name); s.setScientificName(sci); s.setCategory(cat); s.setDescription(desc); s.setCareInstructions(care);
    s.setCo2KgPerYear(co2); s.setMatureHeightMeters(height); s.setMaturityYears(years); s.setGrowthRate(growth); return species.save(s); }
  Tree tr(AppUser nursery,TreeSpecies s,String name,String sci,String cat,double price,int qty){
    Tree t=new Tree(); t.setNursery(nursery); t.setSpecies(s); t.setName(name); t.setScientificName(sci); t.setCategory(cat); t.setPrice(price); t.setQuantity(qty); return trees.save(t); }
  Badge badge(String name,String desc,String icon,String crit,int threshold,int bonus){
    Badge b=new Badge(); b.setName(name); b.setDescription(desc); b.setIcon(icon); b.setCriteriaType(crit); b.setThreshold(threshold); b.setBonusPoints(bonus); return badges.save(b); }
  Reward reward(String name,String desc,int cost,int stock){ Reward r=new Reward(); r.setName(name); r.setDescription(desc); r.setPointsCost(cost); r.setStock(stock); return rewards.save(r); }

  @Override public void run(String... a){
    if(users.count()>0) return;
    cfg.save(new SystemConfig("ECOPOINTS_PER_TREE","150")); cfg.save(new SystemConfig("MAX_SUBMISSION_DISTANCE_METERS","250"));
    cfg.save(new SystemConfig("VERIFICATION_AUTO_EXPIRE_DAYS","14")); cfg.save(new SystemConfig("GEO_BOUNDARY_STRICT_MODE","true")); cfg.save(new SystemConfig("LOW_STOCK_THRESHOLD","100"));

    badge("Sapling Pioneer","Submitted your first plantation","🌱",Const.CRIT_FIRST_SUBMISSION,1,0);
    badge("Carbon Tracker","Logged 3 consumption records","📊",Const.CRIT_CONSUMPTION_LOGS,3,0);
    badge("Forest Guardian","3 verified plantations","🌳",Const.CRIT_VERIFIED_PLANTATIONS,3,50);
    badge("Eco Champion","Reached 10,000 EcoPoints","🏆",Const.CRIT_ECO_POINTS,10000,200);
    reward("Reusable Tote Bag","Organic cotton tote bag",500,100); reward("Free Sapling Voucher","Redeemable at partner nurseries",800,200); reward("EcoSphere T-shirt","Limited edition recycled-cotton tee",2000,50);

    mk("Masud Rana","admin@ecosphere.com",Const.ROLE_ADMIN,"Dhaka",0,0);
    AppUser afia=mk("Afia Anam Mim","afia@ecosphere.com",Const.ROLE_USER,"Mirpur 10",2450,820);
    mk("Sadiya Bani","sadiya@ecosphere.com",Const.ROLE_USER,"Mirpur, Dhaka",14250,980); mk("Fatiha Binte Shahid","fatiha@ecosphere.com",Const.ROLE_USER,"Dhaka",12180,954);
    AppUser ngo=mk("EcoSphere Green Alliance","ngo@ecosphere.com",Const.ROLE_NGO,"Dhaka-1209",0,0);
    AppUser nur=mk("Gardenia Nursery","nursery@ecosphere.com",Const.ROLE_NURSERY,"Gazipur, Dhaka",0,0);
    AppUser nur2=mk("Brac Nursery","bracnursery@ecosphere.com",Const.ROLE_NURSERY,"Savar, Dhaka",0,0);

    TreeSpecies neem=sp("Neem Tree","Azadirachta indica","Medicinal Native","Fast growing drought-resistant native tree with medicinal uses.","Full sun, water weekly for the first year.",20,15,8,"Fast");
    TreeSpecies mango=sp("Mango","Mangifera indica","Fruit","Evergreen fruit tree that also gives dense shade.","Deep watering in dry months, prune after fruiting.",25,20,6,"Medium");
    TreeSpecies rain=sp("Rain Tree","Samanea saman","Shade","Wide-crowned shade tree, excellent for streets and parks.","Plant with 8 m spacing; tolerates most soils.",28,25,10,"Fast");
    TreeSpecies birch=sp("Silver Birch","Betula pendula","Deciduous Hardwood","Slender, light-barked tree for cooler climates.","Moist, well drained soil.",18,20,15,"Medium");
    TreeSpecies oak=sp("Red Oak","Quercus rubra","Deciduous Hardwood","Long-lived hardwood with strong carbon storage.","Acidic soil, full sun.",30,25,20,"Slow");
    sp("Jackfruit","Artocarpus heterophyllus","Fruit","National fruit of Bangladesh; heavy cropper.","Warm, sheltered position.",24,20,6,"Medium");

    tr(nur,birch,"Silver Birch","Betula pendula","Deciduous Hardwood",300,4280); tr(nur,oak,"Red Oak","Quercus rubra","Deciduous Hardwood",350,3410); tr(nur,neem,"Neem Tree","Azadirachta indica","Medicinal Native",500,5120);
    tr(nur2,mango,"Mango","Mangifera indica","Fruit",250,1800); tr(nur2,rain,"Rain Tree","Samanea saman","Shade",180,900);
    tr(nur2,species.findByNameIgnoreCase("Jackfruit").orElse(null),"Jackfruit","Artocarpus heterophyllus","Fruit",320,60);

    PlantationLocation home=new PlantationLocation(); home.setOwner(afia); home.setName("Mirpur 10 rooftop garden"); home.setAddress("Mirpur 10, Dhaka"); home.setDistrict("Dhaka"); home.setLatitude(23.8069); home.setLongitude(90.3687); locations.save(home);

    String[][] c={{"ELECTRICITY","148.5"},{"TRANSPORT","34.2"},{"WATER","620"},{"WASTE","4.5"}};
    for(String[] x:c){ Consumption k=new Consumption(); k.setUser(afia);k.setCategory(x[0]);k.setAmount(Double.parseDouble(x[1]));k.setCo2(Math.round(k.getAmount()*FACTOR.get(x[0])*100)/100.0);cons.save(k); }
    Object[][] p={{"Neem Tree (Azadirachta indica)","Mirpur 10","VERIFIED"},{"Rain Tree (Samanea saman)","Farmgate","PENDING"},{"Mango (Mangifera indica)","Gopalpur, Tangail","PENDING"}};
    for(Object[] x:p){ Plantation pl=new Plantation(); pl.setUser(afia);pl.setTreeName((String)x[0]);pl.setLocation((String)x[1]);pl.setStatus((String)x[2]);pl.setPlantedOn(LocalDate.now().minusDays(5));
      if("VERIFIED".equals(x[2])){pl.setVerifiedBy(ngo);pl.setVerifiedAt(LocalDateTime.now());pl.setPointsAwarded(150);} plants.save(pl); }
    for(String bn:new String[]{"Sapling Pioneer","Carbon Tracker"}){ UserBadge ub=new UserBadge(); ub.setUser(afia); ub.setBadge(badges.findByName(bn).orElseThrow()); userBadges.save(ub); }
  }
}
