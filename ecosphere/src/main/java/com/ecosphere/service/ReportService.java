package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.LocalDateTime; import java.util.*;

/** Complaints and reports raised by users and handled by admins (tickets 24, 32). */
@Service @RequiredArgsConstructor
public class ReportService {
  private static final Set<String> TYPES=Set.of("COMPLAINT","FAKE_PLANTATION","ORDER_ISSUE","ABUSE","BUG");
  private static final Set<String> HANDLED=Set.of(Const.REPORT_IN_REVIEW,Const.REPORT_RESOLVED,Const.REPORT_DISMISSED);
  private final ReportRepo repo; private final UserRepo users; private final NotificationService notifications; private final ActivityLogService log;

  @Transactional
  public Report create(AppUser reporter,String type,String subject,String description,Long againstUserId){
    String t=type==null||type.isBlank()?"COMPLAINT":type.trim().toUpperCase();
    if(!TYPES.contains(t)) throw new IllegalArgumentException("Type must be one of "+TYPES);
    if(subject==null||subject.isBlank()) throw new IllegalArgumentException("Subject is required.");
    if(description==null||description.isBlank()) throw new IllegalArgumentException("Description is required.");
    Report r=new Report(); r.setReporter(reporter); r.setType(t); r.setSubject(subject.trim()); r.setDescription(description.trim());
    if(againstUserId!=null){
      AppUser against=users.findById(againstUserId).orElseThrow(()->new NoSuchElementException("Reported user not found."));
      if(against.getId().equals(reporter.getId())) throw new IllegalArgumentException("You cannot report yourself.");
      r.setAgainst(against); }
    r=repo.save(r);
    notifications.notifyRole(Const.ROLE_ADMIN,"New "+t.toLowerCase().replace('_',' '),r.getSubject(),"REPORT","/admin");
    log.log(reporter,"REPORT_CREATED","Report",r.getId(),t); return r;
  }
  public List<Report> mine(AppUser u){ return repo.findByReporterOrderByCreatedAtDesc(u); }
  public List<Report> aboutMe(AppUser u){ return repo.findByAgainstOrderByCreatedAtDesc(u); }
  public Report get(Long id){ return repo.findById(id).orElseThrow(()->new NoSuchElementException("Report not found.")); }

  // ---- admin (ticket 32)
  public List<Report> adminList(String status){ return status==null||status.isBlank()?repo.findAllByOrderByCreatedAtDesc():repo.findByStatusOrderByCreatedAtDesc(status.toUpperCase()); }
  @Transactional
  public Report handle(Long id,AppUser admin,String status,String response){
    String s=status==null?"":status.trim().toUpperCase();
    if(!HANDLED.contains(s)) throw new IllegalArgumentException("Status must be one of "+HANDLED);
    Report r=get(id);
    if((Const.REPORT_RESOLVED.equals(r.getStatus())||Const.REPORT_DISMISSED.equals(r.getStatus()))) throw new IllegalArgumentException("This report is already closed.");
    if(!Const.REPORT_IN_REVIEW.equals(s)&&(response==null||response.isBlank())) throw new IllegalArgumentException("Add a response before closing the report.");
    r.setStatus(s); r.setAdminResponse(response); r.setHandledBy(admin);
    if(!Const.REPORT_IN_REVIEW.equals(s)) r.setResolvedAt(LocalDateTime.now());
    r=repo.save(r);
    notifications.notify(r.getReporter(),"Report "+s.toLowerCase().replace('_',' '),"\""+r.getSubject()+"\""+(response==null||response.isBlank()?"":": "+response),"REPORT",null);
    log.log(admin,"REPORT_"+s,"Report",r.getId(),null); return r;
  }
  public Map<String,Long> counts(){
    Map<String,Long> m=new LinkedHashMap<>(); for(String s:List.of(Const.REPORT_OPEN,Const.REPORT_IN_REVIEW,Const.REPORT_RESOLVED,Const.REPORT_DISMISSED)) m.put(s,repo.countByStatus(s)); return m;
  }
}
