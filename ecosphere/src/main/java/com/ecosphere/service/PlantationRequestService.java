package com.ecosphere.service;
import com.ecosphere.common.Const; import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.LocalDateTime; import java.util.*;

/** Sapling requests from users / NGOs to nurseries and the nursery's replies (ticket 22). */
@Service @RequiredArgsConstructor
public class PlantationRequestService {
  private final PlantationRequestRepo repo; private final TreeRepo trees; private final NotificationService notifications; private final ActivityLogService log;

  @Transactional
  public PlantationRequest create(AppUser requester,Long treeId,int quantity,String message){
    if(quantity<1||quantity>10000) throw new IllegalArgumentException("Quantity must be between 1 and 10000.");
    Tree t=trees.findById(treeId).orElseThrow(()->new NoSuchElementException("Tree not found."));
    if(!t.isListed()) throw new IllegalArgumentException("This tree is not currently available for requests.");
    PlantationRequest r=new PlantationRequest(); r.setRequester(requester); r.setNursery(t.getNursery()); r.setTree(t); r.setQuantity(quantity); r.setMessage(message);
    r=repo.save(r);
    notifications.notify(t.getNursery(),"New sapling request","From "+requester.getName()+": "+quantity+" x "+t.getName(),"REQUEST","/nursery");
    log.log(requester,"REQUEST_CREATED","PlantationRequest",r.getId(),quantity+" x "+t.getName()); return r;
  }
  public List<PlantationRequest> mine(AppUser requester){ return repo.findByRequesterOrderByCreatedAtDesc(requester); }
  public List<PlantationRequest> forNursery(AppUser nursery){ return repo.findByNurseryOrderByCreatedAtDesc(nursery); }
  public List<PlantationRequest> all(){ return repo.findAllByOrderByCreatedAtDesc(); }

  /** action: ACCEPT, DECLINE or FULFIL. Fulfilling deducts the stock. */
  @Transactional
  public PlantationRequest respond(AppUser nursery,Long id,String action,String response){
    PlantationRequest r=repo.findById(id).orElseThrow(()->new NoSuchElementException("Request not found."));
    if(!r.getNursery().getId().equals(nursery.getId())) throw new SecurityException("Not your request.");
    String cur=r.getStatus(), a=action==null?"":action.trim().toUpperCase();
    switch(a){
      case "ACCEPT" -> { if(!Const.REQ_REQUESTED.equals(cur)) throw new IllegalArgumentException("Only new requests can be accepted."); r.setStatus(Const.REQ_ACCEPTED); }
      case "DECLINE" -> { if(!Const.REQ_REQUESTED.equals(cur)&&!Const.REQ_ACCEPTED.equals(cur)) throw new IllegalArgumentException("This request can no longer be declined."); r.setStatus(Const.REQ_DECLINED); }
      case "FULFIL" -> {
        if(!Const.REQ_ACCEPTED.equals(cur)) throw new IllegalArgumentException("Accept the request before fulfilling it.");
        Tree t=trees.lockById(r.getTree().getId()).orElseThrow();
        if(t.getQuantity()<r.getQuantity()) throw new IllegalArgumentException("Only "+t.getQuantity()+" units in stock.");
        t.setQuantity(t.getQuantity()-r.getQuantity()); trees.save(t); r.setStatus(Const.REQ_FULFILLED); }
      default -> throw new IllegalArgumentException("Action must be ACCEPT, DECLINE or FULFIL.");
    }
    r.setResponse(response); r.setRespondedAt(LocalDateTime.now()); r=repo.save(r);
    notifications.notify(r.getRequester(),"Sapling request "+r.getStatus().toLowerCase(),r.getNursery().getName()+" "+r.getStatus().toLowerCase()+" your request for "+r.getQuantity()+" x "+r.getTree().getName()+(response==null||response.isBlank()?"":": "+response),"REQUEST",null);
    log.log(nursery,"REQUEST_"+r.getStatus(),"PlantationRequest",r.getId(),null); return r;
  }
  @Transactional
  public PlantationRequest cancel(AppUser requester,Long id){
    PlantationRequest r=repo.findById(id).orElseThrow(()->new NoSuchElementException("Request not found."));
    if(!r.getRequester().getId().equals(requester.getId())) throw new SecurityException("Not your request.");
    if(!Const.REQ_REQUESTED.equals(r.getStatus())) throw new IllegalArgumentException("Only new requests can be cancelled.");
    r.setStatus(Const.CANCELLED); r.setRespondedAt(LocalDateTime.now()); return repo.save(r);
  }
}
