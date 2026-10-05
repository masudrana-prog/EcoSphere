package com.ecosphere.controller;
import org.springframework.stereotype.Controller; import org.springframework.web.bind.annotation.GetMapping;

/** Sprint 1 landing page for citizen / NGO / nursery accounts. Replaced by the real dashboards in Sprint 2. */
@Controller
public class HomeController {
  @GetMapping({"/user","/ngo","/nursery"}) String home(){ return "home"; }
}
