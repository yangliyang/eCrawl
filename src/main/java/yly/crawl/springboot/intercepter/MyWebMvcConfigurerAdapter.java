package yly.crawl.springboot.intercepter;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MyWebMvcConfigurerAdapter implements WebMvcConfigurer {
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
		WebMvcConfigurer.super.addResourceHandlers(registry);
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/index");
//        .excludePathPatterns("/hlladmin/login") //登录页
//        .excludePathPatterns("/hlladmin/user/sendEmail") //发送邮箱
//        .excludePathPatterns("/hlladmin/user/register") //用户注册
//        .excludePathPatterns("/hlladmin/user/login"); //用户登录
    
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	
}
