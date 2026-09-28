package com.ecosphere;
import org.springframework.boot.CommandLineRunner; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component; import java.time.*;
@Component
class DataSeeder implements CommandLineRunner {
  UserRepo users; PlantationRepo plants; PasswordEncoder pe;
  DataSeeder(UserRepo u,PlantationRepo p,PasswordEncoder e){users=u;plants=p;pe=e;}
  AppUser mk(String n,String e,String role,String loc,int pts,int gs){ AppUser u=new AppUser(); u.setName(n);u.setEmail(e);u.setRole(role);u.setLocation(loc);u.setPhone("+8801700000000");
    u.setStatus("ACTIVE");u.setEcoPoints(pts);u.setGreenScore(gs);u.setPasswordHash(pe.encode("password123")); return users.save(u); }
  public void run(String... a){
    if(users.count()>0) return;
    mk("Masud Rana","admin@ecosphere.com","ADMIN","Dhaka",0,0);
    AppUser afia=mk("Afia Anam Mim","afia@ecosphere.com","USER","Mirpur 10",2450,820);
    mk("Sadiya Bani","sadiya@ecosphere.com","USER","Mirpur, Dhaka",14250,980); mk("Fatiha Binte Shahid","fatiha@ecosphere.com","USER","Dhaka",12180,954);
    AppUser ngo=mk("EcoSphere Green Alliance","ngo@ecosphere.com","NGO","Dhaka-1209",0,0);
    mk("Gardenia Nursery","nursery@ecosphere.com","NURSERY","Gazipur, Dhaka",0,0);
    mk("Brac Nursery","bracnursery@ecosphere.com","NURSERY","Savar, Dhaka",0,0);
    Object[][] p={{"Neem Tree (Azadirachta indica)","Mirpur 10","VERIFIED"},{"Rain Tree (Samanea saman)","Farmgate","PENDING"},{"Mango (Mangifera indica)","Gopalpur, Tangail","PENDING"}};
  }
}
