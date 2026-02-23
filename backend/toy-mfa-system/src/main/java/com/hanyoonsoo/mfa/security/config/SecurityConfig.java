package com.hanyoonsoo.mfa.security.config;

import com.hanyoonsoo.mfa.security.custom.CustomAccessDeniedHandler;
import com.hanyoonsoo.mfa.security.custom.CustomAuthenticationEntryPoint;
import com.hanyoonsoo.mfa.security.filter.JwtAuthenticationFilter;
import com.hanyoonsoo.mfa.security.mfa.MfaGenerateOneTimeTokenRequestResolver;
import com.hanyoonsoo.mfa.security.mfa.MfaOttAuthenticationFailureHandler;
import com.hanyoonsoo.mfa.security.mfa.MfaOttAuthenticationConverter;
import com.hanyoonsoo.mfa.security.mfa.MfaOttAuthenticationSuccessHandler;
import com.hanyoonsoo.mfa.security.mfa.MfaOttGenerationSuccessHandler;
import com.hanyoonsoo.mfa.security.mfa.SecurityFactor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.GenerateOneTimeTokenFilter;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
@EnableMultiFactorAuthentication(authorities = {})
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final OneTimeTokenService oneTimeTokenService;
    private final MfaGenerateOneTimeTokenRequestResolver mfaGenerateOneTimeTokenRequestResolver;
    private final MfaOttGenerationSuccessHandler mfaOttGenerationSuccessHandler;
    private final MfaOttAuthenticationConverter mfaOttAuthenticationConverter;
    private final MfaOttAuthenticationSuccessHandler mfaOttAuthenticationSuccessHandler;
    private final MfaOttAuthenticationFailureHandler mfaOttAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .logout(AbstractHttpConfigurer::disable)
                .oneTimeTokenLogin(ott -> ott
                        .showDefaultSubmitPage(false)
                        .tokenService(oneTimeTokenService)
                        // 1) Generate OTT
                        .tokenGeneratingUrl("/api/v1/auth/mfa/ott/generate")
                        .generateRequestResolver(mfaGenerateOneTimeTokenRequestResolver)
                        .tokenGenerationSuccessHandler(mfaOttGenerationSuccessHandler)
                        // 2) Verify OTT
                        .loginProcessingUrl("/api/v1/auth/mfa/ott/verify")
                        .authenticationConverter(mfaOttAuthenticationConverter)
                        .successHandler(mfaOttAuthenticationSuccessHandler)
                        .failureHandler(mfaOttAuthenticationFailureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, GenerateOneTimeTokenFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(AllowedPath.permitAllPatterns()).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAuthority(SecurityFactor.PASSWORD)
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").hasAuthority(SecurityFactor.PASSWORD)
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/**")
                        .hasAllAuthorities(SecurityFactor.PASSWORD, SecurityFactor.OTT)
                        .requestMatchers("/api/v1/auth/mfa/**").hasAuthority(SecurityFactor.PASSWORD)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }
}
