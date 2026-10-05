package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.ActivityLogRepo; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import java.util.List;

/** Audit trail for activity monitoring (tickets 24, 30, 34). */
@Service @RequiredArgsConstructor
public class ActivityLogService {
  private final ActivityLogRepo repo;
  public void log(AppUser actor,String action,String entityType,Long entityId,String details){
    ActivityLog l=new ActivityLog();
    l.setActorEmail(actor==null?"system":actor.getEmail()); l.setActorRole(actor==null?"SYSTEM":actor.getRole());
    l.setAction(action); l.setEntityType(entityType); l.setEntityId(entityId);
    l.setDetails(details==null?null:(details.length()>500?details.substring(0,500):details)); repo.save(l);
  }
  public List<ActivityLog> recent(){ return repo.findTop100ByOrderByOccurredAtDesc(); }
  public List<ActivityLog> byActor(String email){ return repo.findTop50ByActorEmailOrderByOccurredAtDesc(email); }
  public List<ActivityLog> byEntityType(String type){ return repo.findTop100ByEntityTypeOrderByOccurredAtDesc(type); }
}
