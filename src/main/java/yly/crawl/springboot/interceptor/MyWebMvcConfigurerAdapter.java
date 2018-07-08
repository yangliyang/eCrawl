package yly.crawl.springboot.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MyWebMvcConfigurerAdapter implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/index")
													.addPathPatterns("/download");
//        .excludePathPatterns("/login") //登录页

    
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	
}
