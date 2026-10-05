package com.ecosphere.api;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException; import java.util.*;

/** Turns service exceptions into clean JSON error responses. */
@RestControllerAdvice(basePackages="com.ecosphere.api")
public class ApiExceptionHandler {
  private ResponseEntity<Map<String,Object>> body(HttpStatus s,String msg){ return ResponseEntity.status(s).body(Map.of("status",s.value(),"error",msg==null?s.getReasonPhrase():msg)); }
  @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<Map<String,Object>> bad(IllegalArgumentException e){ return body(HttpStatus.BAD_REQUEST,e.getMessage()); }
  @ExceptionHandler(NoSuchElementException.class) ResponseEntity<Map<String,Object>> missing(NoSuchElementException e){ return body(HttpStatus.NOT_FOUND,e.getMessage()); }
  @ExceptionHandler(SecurityException.class) ResponseEntity<Map<String,Object>> denied(SecurityException e){ return body(HttpStatus.FORBIDDEN,e.getMessage()); }
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<Map<String,Object>> conflict(DataIntegrityViolationException e){ return body(HttpStatus.CONFLICT,"Duplicate or invalid data."); }
  @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<Map<String,Object>> big(MaxUploadSizeExceededException e){ return body(HttpStatus.PAYLOAD_TOO_LARGE,"File is larger than 10MB."); }
  @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class) ResponseEntity<Map<String,Object>> param(org.springframework.web.bind.MissingServletRequestParameterException e){ return body(HttpStatus.BAD_REQUEST,"Missing parameter: "+e.getParameterName()); }
  @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class) ResponseEntity<Map<String,Object>> json(Exception e){ return body(HttpStatus.BAD_REQUEST,"Malformed request body."); }
}
