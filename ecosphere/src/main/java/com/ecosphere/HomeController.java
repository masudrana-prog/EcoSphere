package com.ecosphere;
import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.GetMapping;
/** Placeholder landing page for portals whose features arrive in a later sprint. */
@Controller
class HomeController {
  @GetMapping({"/ngo","/nursery","/admin"}) String home(Model m){ m.addAttribute("msg","This portal's features are delivered in later sprints."); return "dashboard"; }
}
