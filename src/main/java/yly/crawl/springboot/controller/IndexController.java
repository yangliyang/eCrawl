package yly.crawl.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
	@RequestMapping("/index")
	public ModelAndView showIndexPage(){
		ModelAndView mav = new ModelAndView("index");
		return mav;
	}
	@RequestMapping("/")
	public String jump(){
		return "redirect:index";
	}
}
