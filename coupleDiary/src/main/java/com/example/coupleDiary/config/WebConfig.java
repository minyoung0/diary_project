package com.example.coupleDiary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/profileImg/**")
                .addResourceLocations(  "file:///C:/uploads/profile/",        // 업로드된 이미지 경로
                        "classpath:/static/profileimg/");
    }
}
