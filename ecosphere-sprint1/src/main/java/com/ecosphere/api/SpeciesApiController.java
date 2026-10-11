package com.ecosphere.api;
import com.ecosphere.entity.*; import com.ecosphere.service.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.util.List;

/** Public tree & sapling information and the buy catalogue (tickets 17, 20). */
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class SpeciesApiController {
  private final TreeSpeciesService species; private final TreeService trees;
  @GetMapping("/species") List<TreeSpecies> species(@RequestParam(required=false) String q,@RequestParam(required=false) String category){ return species.browse(q,category); }
  @GetMapping("/species/categories") List<String> categories(){ return species.categories(); }
  @GetMapping("/species/{id}") TreeSpecies detail(@PathVariable Long id){ return species.get(id); }
  @GetMapping("/species/{id}/sellers") List<Tree> sellers(@PathVariable Long id){ return species.sellers(id); }
  @GetMapping("/catalogue") List<Tree> catalogue(@RequestParam(required=false) String q,@RequestParam(required=false) String category){ return trees.catalogue(q,category); }
}
