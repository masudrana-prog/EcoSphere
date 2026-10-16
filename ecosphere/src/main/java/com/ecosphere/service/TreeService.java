package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** Nursery tree / sapling management (ticket 20) and the public buy catalogue. */
@Service @RequiredArgsConstructor
public class TreeService {
  private final TreeRepo trees; private final TreeOrderRepo orders; private final TreeSpeciesRepo species; private final ActivityLogService log;

  public List<Tree> mine(AppUser nursery){ return trees.findByNurseryOrderByIdDesc(nursery); }
  public Tree owned(AppUser nursery,Long id){
    Tree t=trees.findById(id).orElseThrow(()->new NoSuchElementException("Tree not found."));
    if(!t.getNursery().getId().equals(nursery.getId())) throw new SecurityException("Not your tree."); return t;
  }
  /** Public catalogue used by "Buy Tree": only listed lines, optional search and category filter. */
  public List<Tree> catalogue(String q,String category){
    String needle=q==null?"":q.trim().toLowerCase();
    return trees.findByListedTrueOrderByNameAsc().stream()
      .filter(t->needle.isEmpty()||t.getName().toLowerCase().contains(needle)||(t.getScientificName()!=null&&t.getScientificName().toLowerCase().contains(needle)))
      .filter(t->category==null||category.isBlank()||category.equals(t.getCategory())).toList();
  }
  public List<String> categories(){ return trees.findAll().stream().map(Tree::getCategory).filter(Objects::nonNull).distinct().toList(); }

  @Transactional
  public Tree add(AppUser nursery,String name,String scientificName,String category,double price,int quantity,Long speciesId){
    if(name==null||name.isBlank()) throw new IllegalArgumentException("Species name is required.");
    if(price<0) throw new IllegalArgumentException("Price cannot be negative.");
    Tree t=new Tree(); t.setNursery(nursery); t.setName(name.trim()); t.setScientificName(scientificName); t.setCategory(category==null||category.isBlank()?"General":category);
    t.setPrice(price); t.setQuantity(Math.max(0,quantity));
    if(speciesId!=null) t.setSpecies(species.findById(speciesId).orElseThrow(()->new NoSuchElementException("Species not found.")));
    t=trees.save(t); log.log(nursery,"TREE_ADDED","Tree",t.getId(),t.getName()); return t;
  }
  @Transactional
  public Tree update(AppUser nursery,Long id,String name,String scientificName,String category,Double price){
    Tree t=owned(nursery,id);
    if(name!=null&&!name.isBlank()) t.setName(name.trim());
    if(scientificName!=null) t.setScientificName(scientificName);
    if(category!=null&&!category.isBlank()) t.setCategory(category);
    if(price!=null){ if(price<0) throw new IllegalArgumentException("Price cannot be negative."); t.setPrice(price); }
    return trees.save(t);
  }
  @Transactional
  public Tree setStock(AppUser nursery,Long id,int quantity){
    Tree t=owned(nursery,id); t.setQuantity(Math.max(0,quantity)); t=trees.save(t);
    log.log(nursery,"STOCK_UPDATED","Tree",id,t.getName()+" -> "+t.getQuantity()); return t;
  }
  /** Lines that have orders are zeroed and hidden instead of deleted so order history keeps working. */
  @Transactional
  public void remove(AppUser nursery,Long id){
    Tree t=owned(nursery,id);
    if(orders.existsByTree(t)){ t.setQuantity(0); t.setListed(false); trees.save(t); } else trees.delete(t);
    log.log(nursery,"TREE_REMOVED","Tree",id,t.getName());
  }
}
