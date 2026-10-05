package com.ecosphere.service;
import org.springframework.stereotype.Service; import org.springframework.web.multipart.MultipartFile;
import java.io.IOException; import java.nio.file.*; import java.util.*;

/** Saves uploaded images under ./uploads and returns the public URL. */
@Service
public class FileStorageService {
  private static final Set<String> TYPES=Set.of("image/jpeg","image/png","image/webp","image/gif");
  public String storeImage(MultipartFile f) throws IOException {
    if(f==null||f.isEmpty()) return null;
    if(f.getContentType()==null||!TYPES.contains(f.getContentType())) throw new IllegalArgumentException("Only JPEG, PNG, WEBP or GIF images are allowed.");
    Files.createDirectories(Path.of("uploads"));
    String original=f.getOriginalFilename()==null?"photo":f.getOriginalFilename();
    String n=UUID.randomUUID()+"_"+original.replaceAll("[^a-zA-Z0-9._-]","_");
    Files.copy(f.getInputStream(),Path.of("uploads",n)); return "/uploads/"+n;
  }
}
