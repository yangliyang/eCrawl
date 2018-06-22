package yly.crawl.springboot.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import yly.crawl.springboot.util.CookieUtil;

@Component
public class LoginInterceptor implements HandlerInterceptor {
 
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Cookie member = CookieUtil.get(request, "ipb_member_id");
		Cookie pass = CookieUtil.get(request, "ipb_pass_hash");
		
		if(member == null || pass == null){
			response.sendRedirect("/login");
			return false;
		}
		return true;
	}

}
