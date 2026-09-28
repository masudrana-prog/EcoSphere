package com.ecosphere;
import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; import java.io.IOException; import java.nio.file.*; import java.security.Principal; import java.time.LocalDate; import java.util.*;
@Controller
class UserController {
  UserRepo users; PlantationRepo plants;
  UserController(UserRepo u,PlantationRepo p){users=u;plants=p;}
  @GetMapping("/user") String dash(Model m,Principal p){
    AppUser me=users.findByEmail(p.getName()).get(); var pl=plants.findByUserOrderBySubmittedAtDesc(me);
    m.addAttribute("plants",pl);
    m.addAttribute("verified",pl.stream().filter(x->"VERIFIED".equals(x.getStatus())).count());
    m.addAttribute("pending",pl.stream().filter(x->"PENDING".equals(x.getStatus())).count());
    return "user";
  }
  @PostMapping("/user/plantation")
  String submit(@RequestParam String treeName,@RequestParam String location,@RequestParam LocalDate plantedOn,@RequestParam(required=false) Double lat,@RequestParam(required=false) Double lng,
      @RequestParam MultipartFile photo,Principal p) throws IOException {
    Plantation x=new Plantation(); x.setUser(users.findByEmail(p.getName()).get()); x.setTreeName(treeName);x.setLocation(location);x.setPlantedOn(plantedOn);x.setLat(lat);x.setLng(lng);
    if(!photo.isEmpty()){ Files.createDirectories(Path.of("uploads")); String n=UUID.randomUUID()+"_"+photo.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]","_"); Files.copy(photo.getInputStream(),Path.of("uploads",n)); x.setPhotoUrl("/uploads/"+n); }
    plants.save(x); return "redirect:/user";
  }
}
