package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** In-app notifications and admin broadcasts (tickets 23, 33). */
@Service @RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepo repo; private final UserRepo users;

  public Notification notify(AppUser to,String title,String message,String type,String link){
    if(to==null) return null;
    Notification n=new Notification(); n.setRecipient(to); n.setTitle(title); n.setMessage(message); n.setType(type==null?"INFO":type); n.setLink(link);
    return repo.save(n);
  }
  /** Notify every ACTIVE user with the role. */
  public void notifyRole(String role,String title,String message,String type,String link){
    for(AppUser u:users.findByRoleOrderByCreatedAtDesc(role)) if("ACTIVE".equals(u.getStatus())) notify(u,title,message,type,link);
  }
  /** Admin broadcast: role = USER/NGO/NURSERY/ADMIN or null/"ALL" for everyone active. Returns recipients count. */
  @Transactional
  public int broadcast(String role,String title,String message){
    if(title==null||title.isBlank()||message==null||message.isBlank()) throw new IllegalArgumentException("Title and message are required.");
    List<AppUser> targets=(role==null||role.isBlank()||"ALL".equalsIgnoreCase(role))?users.findByStatus("ACTIVE"):users.findByRoleOrderByCreatedAtDesc(role.toUpperCase()).stream().filter(u->"ACTIVE".equals(u.getStatus())).toList();
    for(AppUser u:targets) notify(u,title.trim(),message.trim(),"BROADCAST",null);
    return targets.size();
  }
  public List<Notification> mine(AppUser u){ return repo.findByRecipientOrderByCreatedAtDesc(u); }
  public long unreadCount(AppUser u){ return repo.countByRecipientAndSeenFalse(u); }
  @Transactional public Notification markRead(Long id,AppUser u){
    Notification n=repo.findById(id).orElseThrow(()->new NoSuchElementException("Notification not found."));
    if(!n.getRecipient().getId().equals(u.getId())) throw new SecurityException("Not your notification.");
    n.setSeen(true); return repo.save(n);
  }
  @Transactional public int markAllRead(AppUser u){ List<Notification> l=repo.findByRecipientAndSeenFalse(u); l.forEach(n->n.setSeen(true)); repo.saveAll(l); return l.size(); }
  @Transactional public void delete(Long id,AppUser u){
    Notification n=repo.findById(id).orElseThrow(()->new NoSuchElementException("Notification not found."));
    if(!n.getRecipient().getId().equals(u.getId())) throw new SecurityException("Not your notification.");
    repo.delete(n);
  }
  // admin side (ticket 33)
  public List<Notification> all(){ return repo.findAllByOrderByCreatedAtDesc(); }
  public List<Notification> broadcasts(){ return repo.findByTypeOrderByCreatedAtDesc("BROADCAST"); }
  public void adminDelete(Long id){ repo.deleteById(id); }
}
