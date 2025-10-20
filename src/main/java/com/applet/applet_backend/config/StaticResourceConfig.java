package com.applet.applet_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos estáticos del frontend
        registry.addResourceHandler("/audio/**")
                .addResourceLocations("file:../frontend/public/audio/");
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:../frontend/public/images/");
    }
}