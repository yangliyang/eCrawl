package yly.crawl.springboot.controller;

import java.text.DateFormat;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloController {
	@RequestMapping("/hello")
	public ModelAndView hello(ModelAndView m){
		m.setViewName("hello");

		m.addObject("now", DateFormat.getDateTimeInstance().format(new Date()));
		return m;
	}

}
