package com.hanyoonsoo.mfa.config;

import com.hanyoonsoo.mfa.security.config.CorsAllowedOriginsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CorsAllowedOriginsProperties corsAllowedOriginsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowedOrigins(corsAllowedOriginsProperties.getOrigins().toArray(String[]::new))
                .allowCredentials(true)
                .exposedHeaders("Authorization");
    }
}
