package com.devlatte.devroom.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import com.devlatte.devroom.security.JwtAuthorizationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@Configuration
@ComponentScan(basePackages = "com.devlatte.devroom.security")
public class SecurityConfig {

    @Value("${jwt.token.URL}")
    private String keyURL;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .cors(corsCustomizer -> corsCustomizer
                        .configurationSource(corsConfigurationSource())
                )
                .authorizeHttpRequests(auth ->{
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/error").permitAll()
                        // 보안을 위해 k8s에 직접적으로 요청하는 API는 전부 deny
                        .requestMatchers("/core/**").denyAll()
                        .requestMatchers("/pod/{id}","/service/{id}","/deploy/{id}").access(new WebExpressionAuthorizationManager("hasAuthority('ID_'+#id) and hasAuthority('ROLE_Student')"))
                        .requestMatchers("/class/{id}/**").access(new WebExpressionAuthorizationManager("hasAuthority('ID_'+#id) and hasAuthority('ROLE_Professor')"))
                        .anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt->{
                            jwt.jwkSetUri(keyURL);
                            jwt.jwtAuthenticationConverter(jwtAuthorizationConverter());
                        })
                )
                .sessionManagement((sessionConfig) ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig.accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }

    @Bean
    public JwtAuthorizationConverter jwtAuthorizationConverter(){
        return new JwtAuthorizationConverter();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://home.devroom.online/onboarding"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(Duration.ofMinutes(5L));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

    private final AccessDeniedHandler accessDeniedHandler = (((request, response, accessDeniedException) -> {
        ObjectMapper objectMapper = new ObjectMapper();

        String responseBody = objectMapper.writeValueAsString(
                ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied")
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }));

}



