package yly.crawl.springboot.controller;


import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import yly.crawl.springboot.util.CookieUtil;
import yly.crawl.springboot.util.Login;

@Controller
public class IndexController {
	
	private static final String MEMBER = "ipb_member_id";
	private static final String PASS = "ipb_pass_hash";
	@RequestMapping("/index")
	public ModelAndView showIndexPage(HttpServletRequest req){
		
		ModelAndView mav = new ModelAndView("index");
		Cookie mCookie = CookieUtil.get(req, MEMBER);
		Cookie pCookie = CookieUtil.get(req, PASS);
		String cookie1 = mCookie.getValue();
		String cookie2 = pCookie.getValue();
		String localhost = null;
		try {
			InetAddress myIp = InetAddress.getLocalHost();
			localhost = myIp.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if(localhost==null){
			localhost = "127.0.0.1";
		}
		mav.getModel().put("cookie", "ipb_member_id:"+cookie1+";ipb_pass_hash:"+cookie2 );
		mav.getModel().put("localhost", localhost);
		return mav;
	}
	@RequestMapping("/")
	public String jump(){
		return "redirect:index";
	}
	@RequestMapping("/login")
	public String login(){
		return "login";
	}
	@RequestMapping("/logout")
	public String logout(HttpServletRequest req, HttpServletResponse res){
		Cookie member = CookieUtil.get(req, MEMBER);
		Cookie pass = CookieUtil.get(req, PASS);
		if(member!=null){
			CookieUtil.set(res, MEMBER, null, 0);
		}
		if(pass!=null){
			CookieUtil.set(res, PASS, null, 0);
		}
		
		return "redirect:login";
	}
	@RequestMapping("/loginVerify")
	public String loginVerify(String username, String password, HttpServletResponse res){
		if(username==null || password==null){
			return "redirect:login";
		}
		String[] info = eloginVerify(username, password);
		final int maxAge = 3600*24*365; //一年
		if(info[0].equals("true")){
			
			CookieUtil.set(res, MEMBER, info[1], maxAge);
			CookieUtil.set(res, PASS, info[2], maxAge);
			
			
			return "redirect:success";
			//res.sendRedirect("/index?ipb_member_id="+info[1]+"&ipb_pass_hash="+info[2]);
		}
		return "redirect:login";
	}
	@RequestMapping("/success")
	public String success(){
		return "success";
	}
	public String[] eloginVerify(String name, String password){
		return Login.eLogin(name, password);
	}
}
