package com.ecosphere.service;
import com.ecosphere.entity.*; import com.ecosphere.repository.*; import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;

/** Saved planting sites with GPS coordinates (ticket 6). */
@Service @RequiredArgsConstructor
public class PlantationLocationService {
  private final PlantationLocationRepo repo; private final PlantationRepo plantations;

  public static void validateCoordinates(Double lat,Double lng){
    if((lat==null)!=(lng==null)) throw new IllegalArgumentException("Provide both latitude and longitude.");
    if(lat!=null&&(lat<-90||lat>90)) throw new IllegalArgumentException("Latitude must be between -90 and 90.");
    if(lng!=null&&(lng<-180||lng>180)) throw new IllegalArgumentException("Longitude must be between -180 and 180.");
  }
  public List<PlantationLocation> mine(AppUser u){ return repo.findByOwnerAndActiveTrueOrderByNameAsc(u); }
  public PlantationLocation get(Long id,AppUser u){
    PlantationLocation l=repo.findById(id).orElseThrow(()->new NoSuchElementException("Location not found."));
    if(!l.getOwner().getId().equals(u.getId())) throw new SecurityException("Not your location."); return l;
  }
  @Transactional
  public PlantationLocation save(AppUser owner,Long id,String name,String address,String district,Double lat,Double lng){
    if(name==null||name.isBlank()) throw new IllegalArgumentException("Location name is required.");
    validateCoordinates(lat,lng);
    PlantationLocation l=id==null?new PlantationLocation():get(id,owner);
    l.setOwner(owner); l.setName(name.trim()); l.setAddress(address); l.setDistrict(district); l.setLatitude(lat); l.setLongitude(lng);
    return repo.save(l);
  }
  /** Locations already attached to a plantation are archived rather than deleted so history stays intact. */
  @Transactional
  public void delete(AppUser owner,Long id){
    PlantationLocation l=get(id,owner);
    if(plantations.findBySavedLocation(l).isEmpty()) repo.delete(l); else { l.setActive(false); repo.save(l); }
  }
}
