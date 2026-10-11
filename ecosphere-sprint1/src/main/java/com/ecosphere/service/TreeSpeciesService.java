package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** Tree & sapling reference information: public browsing (ticket 17) and admin management (ticket 29). */
@Service @RequiredArgsConstructor
public class TreeSpeciesService {
  private final TreeSpeciesRepo repo; private final TreeRepo trees; private final ActivityLogService log;

  // ---- browsing
  public List<TreeSpecies> browse(String q,String category){
    String needle=q==null?"":q.trim().toLowerCase();
    return repo.findByActiveTrueOrderByNameAsc().stream()
      .filter(s->needle.isEmpty()||s.getName().toLowerCase().contains(needle)||(s.getScientificName()!=null&&s.getScientificName().toLowerCase().contains(needle)))
      .filter(s->category==null||category.isBlank()||category.equalsIgnoreCase(s.getCategory())).toList();
  }
  public TreeSpecies get(Long id){ return repo.findById(id).orElseThrow(()->new NoSuchElementException("Species not found.")); }
  public List<String> categories(){ return repo.findByActiveTrueOrderByNameAsc().stream().map(TreeSpecies::getCategory).filter(Objects::nonNull).distinct().sorted().toList(); }
  /** Which nurseries currently sell this species. */
  public List<Tree> sellers(Long speciesId){ return trees.findBySpecies(get(speciesId)).stream().filter(Tree::isAvailable).toList(); }

  // ---- admin management (ticket 29)
  public List<TreeSpecies> all(){ return repo.findAll(); }
  @Transactional
  public TreeSpecies save(Long id,TreeSpecies in,AppUser admin){
    if(in.getName()==null||in.getName().isBlank()) throw new IllegalArgumentException("Species name is required.");
    if(in.getCo2KgPerYear()<0) throw new IllegalArgumentException("CO2 absorption cannot be negative.");
    TreeSpecies s=id==null?new TreeSpecies():get(id);
    repo.findByNameIgnoreCase(in.getName().trim()).filter(x->!x.getId().equals(s.getId())).ifPresent(x->{ throw new IllegalArgumentException("A species with this name already exists."); });
    s.setName(in.getName().trim()); s.setScientificName(in.getScientificName()); s.setCategory(in.getCategory()); s.setGrowthRate(in.getGrowthRate());
    s.setDescription(in.getDescription()); s.setCareInstructions(in.getCareInstructions()); s.setHabitat(in.getHabitat()); s.setImageUrl(in.getImageUrl());
    s.setCo2KgPerYear(in.getCo2KgPerYear()); s.setMatureHeightMeters(in.getMatureHeightMeters()); s.setMaturityYears(in.getMaturityYears()); s.setActive(in.isActive());
    TreeSpecies saved=repo.save(s); log.log(admin,id==null?"SPECIES_CREATED":"SPECIES_UPDATED","TreeSpecies",saved.getId(),saved.getName()); return saved;
  }
  @Transactional
  public void delete(Long id,AppUser admin){
    TreeSpecies s=get(id);
    if(!trees.findBySpecies(s).isEmpty()){ s.setActive(false); repo.save(s); } else repo.delete(s); // keep species still referenced by stock
    log.log(admin,"SPECIES_REMOVED","TreeSpecies",id,s.getName());
  }
}
