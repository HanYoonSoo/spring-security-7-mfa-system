package com.hanyoonsoo.mfa.security.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "cors.allowed")
public class CorsAllowedOriginsProperties {
    private List<String> origins = new ArrayList<>();
}
