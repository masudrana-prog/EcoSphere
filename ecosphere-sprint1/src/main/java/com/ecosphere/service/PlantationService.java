package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.dto.*; import com.ecosphere.entity.*; import com.ecosphere.repository.*;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; import java.io.IOException; import java.time.*; import java.util.*;

/** Tree plantation submission, verification, status tracking and admin oversight (tickets 2, 5, 10, 11, 30). */
@Service @RequiredArgsConstructor
public class PlantationService {
  private final PlantationRepo plants; private final UserRepo users; private final PlantationLocationService locations; private final FileStorageService files;
  private final ConfigService config;
  private final NotificationService notifications; private final ActivityLogService log;

  // ---------------- submission (ticket 5)
  @Transactional
  public Plantation submit(AppUser user,String treeName,String location,LocalDate plantedOn,Double lat,Double lng,Long savedLocationId,MultipartFile photo) throws IOException {
    if(treeName==null||treeName.isBlank()) throw new IllegalArgumentException("Tree / species name is required.");
    if(plantedOn==null) throw new IllegalArgumentException("Planting date is required.");
    if(plantedOn.isAfter(LocalDate.now())) throw new IllegalArgumentException("Planting date cannot be in the future.");
    if(plantedOn.isBefore(LocalDate.now().minusYears(1))) throw new IllegalArgumentException("Plantations older than one year cannot be submitted.");
    PlantationLocation saved=null;
    if(savedLocationId!=null){ saved=locations.get(savedLocationId,user);
      if(location==null||location.isBlank()) location=saved.getName()+(saved.getAddress()==null?"":", "+saved.getAddress());
      if(lat==null){ lat=saved.getLatitude(); lng=saved.getLongitude(); } }
    if(location==null||location.isBlank()) throw new IllegalArgumentException("Location is required.");
    PlantationLocationService.validateCoordinates(lat,lng);
    if(photo==null||photo.isEmpty()) throw new IllegalArgumentException("A photo of the planted tree is required as proof.");
    Plantation p=new Plantation(); p.setUser(user); p.setTreeName(treeName.trim()); p.setLocation(location.trim()); p.setPlantedOn(plantedOn);
    p.setLat(lat); p.setLng(lng); p.setSavedLocation(saved); p.setPhotoUrl(files.storeImage(photo));
    p=plants.save(p);
    log.log(user,"PLANTATION_SUBMITTED","Plantation",p.getId(),p.getTreeName());
    notifications.notifyRole(Const.ROLE_NGO,"New plantation to verify",user.getName()+" submitted "+p.getTreeName()+" at "+p.getLocation()+".","PLANTATION","/ngo");
    return p;
  }

  // ---------------- tracking (ticket 10)
  public List<Plantation> mine(AppUser u){ return plants.findByUserOrderBySubmittedAtDesc(u); }
  public List<Plantation> mine(AppUser u,String status){
    List<Plantation> all=mine(u); if(status==null||status.isBlank()) return all;
    return all.stream().filter(p->p.getStatus().equalsIgnoreCase(status)).toList();
  }
  public PlantationTracking track(Long id,AppUser viewer){
    Plantation p=plants.findById(id).orElseThrow(()->new NoSuchElementException("Plantation not found."));
    boolean owner=p.getUser().getId().equals(viewer.getId());
    if(!owner&&!Const.ROLE_ADMIN.equals(viewer.getRole())&&!Const.ROLE_NGO.equals(viewer.getRole())) throw new SecurityException("Not your plantation.");
    List<TrackingStep> steps=new ArrayList<>();
    steps.add(new TrackingStep("Submitted",true,p.getSubmittedAt(),"Photo and location received"));
    boolean reviewed=!Const.PENDING.equals(p.getStatus());
    steps.add(new TrackingStep("Under NGO review",reviewed||Const.PENDING.equals(p.getStatus()),null,reviewed?"Review completed":"Waiting for an NGO to inspect"));
    if(Const.VERIFIED.equals(p.getStatus())) steps.add(new TrackingStep("Verified",true,p.getVerifiedAt(),"+"+p.getPointsAwarded()+" EcoPoints awarded"));
    else if(Const.REJECTED.equals(p.getStatus())) steps.add(new TrackingStep("Rejected",true,p.getVerifiedAt(),p.getRejectReason()));
    else steps.add(new TrackingStep("Verified",false,null,"EcoPoints are awarded after verification"));
    return new PlantationTracking(p,p.getStatus(),steps);
  }

  // ---------------- NGO verification (ticket 11)
  public List<Plantation> pendingQueue(){ return plants.findByStatusOrderBySubmittedAtDesc(Const.PENDING); }
  public List<Plantation> reviewed(){ return plants.findByStatusNotOrderBySubmittedAtDesc(Const.PENDING); }
  public List<Plantation> reviewedBy(AppUser ngo){ return plants.findByVerifiedByOrderByVerifiedAtDesc(ngo); }

  /** Awards EcoPoints exactly once: a non-PENDING plantation can never be approved again. */
  @Transactional
  public Plantation approve(Long id,AppUser reviewer){
    Plantation p=plants.findById(id).orElseThrow(()->new NoSuchElementException("Plantation not found."));
    if(!Const.PENDING.equals(p.getStatus())) throw new IllegalArgumentException("This plantation has already been reviewed.");
    int pts=config.getInt(Const.CFG_POINTS_PER_TREE,150);
    p.setStatus(Const.VERIFIED); p.setPointsAwarded(pts); p.setVerifiedAt(LocalDateTime.now()); p.setVerifiedBy(reviewer); plants.save(p);
    AppUser owner=p.getUser(); owner.setEcoPoints(owner.getEcoPoints()+pts); owner.setGreenScore(Math.min(1000,owner.getGreenScore()+pts/5)); users.save(owner);
    notifications.notify(owner,"Plantation verified","Your "+p.getTreeName()+" was verified. +"+pts+" EcoPoints!","PLANTATION","/user");
    log.log(reviewer,"PLANTATION_VERIFIED","Plantation",p.getId(),p.getTreeName()+" +"+pts); return p;
  }
  @Transactional
  public Plantation reject(Long id,AppUser reviewer,String reason){
    if(reason==null||reason.isBlank()) throw new IllegalArgumentException("A rejection reason is required.");
    Plantation p=plants.findById(id).orElseThrow(()->new NoSuchElementException("Plantation not found."));
    if(!Const.PENDING.equals(p.getStatus())) throw new IllegalArgumentException("This plantation has already been reviewed.");
    p.setStatus(Const.REJECTED); p.setRejectReason(reason.trim()); p.setVerifiedAt(LocalDateTime.now()); p.setVerifiedBy(reviewer); plants.save(p);
    notifications.notify(p.getUser(),"Plantation rejected","Your "+p.getTreeName()+" was rejected: "+reason.trim(),"PLANTATION","/user");
    log.log(reviewer,"PLANTATION_REJECTED","Plantation",p.getId(),reason); return p;
  }

  // ---------------- admin management / monitoring (tickets 2, 30)
  public List<Plantation> adminList(String status){
    return (status==null||status.isBlank())?plants.findAllByOrderBySubmittedAtDesc():plants.findByStatusOrderBySubmittedAtDesc(status.toUpperCase());
  }
  public Plantation get(Long id){ return plants.findById(id).orElseThrow(()->new NoSuchElementException("Plantation not found.")); }
  /** Admin can re-open a wrongly judged submission. EcoPoints given earlier are taken back so they are never double counted. */
  @Transactional
  public Plantation adminReopen(Long id,AppUser admin,String note){
    Plantation p=get(id);
    if(Const.PENDING.equals(p.getStatus())) throw new IllegalArgumentException("Plantation is already pending.");
    if(Const.VERIFIED.equals(p.getStatus())){ AppUser o=p.getUser(); o.setEcoPoints(Math.max(0,o.getEcoPoints()-p.getPointsAwarded())); o.setGreenScore(Math.max(0,o.getGreenScore()-p.getPointsAwarded()/5)); users.save(o); }
    p.setStatus(Const.PENDING); p.setPointsAwarded(0); p.setRejectReason(null); p.setVerifiedAt(null); p.setVerifiedBy(null); plants.save(p);
    notifications.notify(p.getUser(),"Plantation re-opened","Your "+p.getTreeName()+" is back in the review queue.","PLANTATION","/user");
    log.log(admin,"PLANTATION_REOPENED","Plantation",p.getId(),note); return p;
  }
  @Transactional
  public void adminDelete(Long id,AppUser admin){
    Plantation p=get(id);
    if(Const.VERIFIED.equals(p.getStatus())) adminReopen(id,admin,"deleted by admin");
    plants.delete(p); log.log(admin,"PLANTATION_DELETED","Plantation",id,p.getTreeName());
  }
  public Map<String,Long> statusCounts(){
    Map<String,Long> m=new LinkedHashMap<>(); for(String s:List.of(Const.PENDING,Const.VERIFIED,Const.REJECTED)) m.put(s,plants.countByStatus(s)); m.put("TOTAL",plants.count()); return m;
  }
}
