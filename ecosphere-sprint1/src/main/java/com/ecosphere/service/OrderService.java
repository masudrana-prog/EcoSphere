package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** Buying trees: stock reservation, bKash / COD payment validation, order lifecycle. */
@Service @RequiredArgsConstructor
public class OrderService {
  static final Set<String> PAY=Set.of("COD","BKASH");
  private final TreeRepo trees; private final TreeOrderRepo orders; private final NotificationService notifications; private final ActivityLogService log;

  public List<TreeOrder> mine(AppUser buyer){ return orders.findByBuyerOrderByCreatedAtDesc(buyer); }
  public List<TreeOrder> forNursery(AppUser n){ return orders.findByNurseryOrderByCreatedAtDesc(n); }

  @Transactional
  public TreeOrder place(AppUser buyer,Long treeId,int qty,String address,String phone,String pay,String bkashSender,String trxId){
    if(qty<1||qty>10000) throw new IllegalArgumentException("Quantity must be between 1 and 10000.");
    if(address==null||address.isBlank()) throw new IllegalArgumentException("Delivery address is required.");
    if(phone==null||phone.isBlank()) throw new IllegalArgumentException("Contact phone is required.");
    if(!PAY.contains(pay)) throw new IllegalArgumentException("Invalid payment method.");
    String sender=null, trx=null;
    if("BKASH".equals(pay)){
      sender=bkashSender==null?"":bkashSender.replaceAll("[\\s-]","");
      if(!sender.matches("^(\\+?880|0)?1[3-9]\\d{8}$")) throw new IllegalArgumentException("Enter a valid bKash number (e.g. 01712345678).");
      trx=trxId==null?"":trxId.trim().toUpperCase();
      if(!trx.matches("^[A-Z0-9]{6,20}$")) throw new IllegalArgumentException("Enter the bKash Transaction ID (6-20 letters/digits).");
      if(orders.existsByTrxId(trx)) throw new IllegalArgumentException("This bKash transaction ID has already been used.");
    }
    Tree t=trees.lockById(treeId).orElseThrow(()->new IllegalArgumentException("Tree not found."));
    if(!t.isListed()) throw new IllegalArgumentException("This tree is not currently on sale.");
    if(t.getQuantity()<qty) throw new IllegalArgumentException("Only "+t.getQuantity()+" units of "+t.getName()+" are available.");
    t.setQuantity(t.getQuantity()-qty); trees.save(t);
    TreeOrder o=new TreeOrder(); o.setBuyer(buyer);o.setNursery(t.getNursery());o.setTree(t);o.setQuantity(qty);o.setUnitPrice(t.getPrice());
    o.setTotal(Math.round(qty*t.getPrice()*100)/100.0);o.setDeliveryAddress(address.trim());o.setPhone(phone.trim());o.setPaymentMethod(pay);o.setBkashSender(sender);o.setTrxId(trx);
    o=orders.save(o);
    notifications.notify(t.getNursery(),"New order #"+o.getId(),buyer.getName()+" ordered "+qty+" x "+t.getName()+" (৳"+o.getTotal()+").","ORDER","/nursery");
    log.log(buyer,"ORDER_PLACED","TreeOrder",o.getId(),qty+" x "+t.getName());
    if(t.isLowStock()) notifications.notify(t.getNursery(),"Low stock: "+t.getName(),"Only "+t.getQuantity()+" units left.","INFO","/nursery");
    return o;
  }

  /** nurseryActor=true: nursery may confirm/deliver/cancel its own orders; false: buyer may cancel only a PENDING order. */
  @Transactional
  public void changeStatus(Long id,String actorEmail,boolean nurseryActor,String next){
    TreeOrder o=orders.findById(id).orElseThrow(()->new IllegalArgumentException("Order not found."));
    String cur=o.getStatus();
    if(nurseryActor){
      if(!o.getNursery().getEmail().equals(actorEmail)) throw new IllegalArgumentException("Not your order.");
      boolean ok=("PENDING".equals(cur)&&Set.of("CONFIRMED","CANCELLED").contains(next))||("CONFIRMED".equals(cur)&&Set.of("DELIVERED","CANCELLED").contains(next));
      if(!ok) throw new IllegalArgumentException("Invalid status change.");
    } else {
      if(!o.getBuyer().getEmail().equals(actorEmail)) throw new IllegalArgumentException("Not your order.");
      if(!"PENDING".equals(cur)||!"CANCELLED".equals(next)) throw new IllegalArgumentException("Only pending orders can be cancelled.");
    }
    if("CANCELLED".equals(next)){ // give the stock back
      Tree t=trees.lockById(o.getTree().getId()).orElseThrow(); t.setQuantity(t.getQuantity()+o.getQuantity()); trees.save(t);
    }
    o.setStatus(next); orders.save(o);
    if(nurseryActor) notifications.notify(o.getBuyer(),"Order #"+o.getId()+" "+next.toLowerCase(),"Your order for "+o.getTree().getName()+" is now "+next+".","ORDER","/user/orders");
    else notifications.notify(o.getNursery(),"Order #"+o.getId()+" cancelled","The buyer cancelled the order for "+o.getTree().getName()+".","ORDER","/nursery");
    log.log(null,"ORDER_"+next,"TreeOrder",o.getId(),actorEmail);
  }
}
