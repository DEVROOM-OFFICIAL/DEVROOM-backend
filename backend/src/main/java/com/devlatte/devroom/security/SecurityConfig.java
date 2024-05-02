package com.devlatte.devroom.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import com.devlatte.devroom.security.JwtAuthorizationConverter;

@Slf4j
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@Configuration
@ComponentScan(basePackages = "com.devlatte.devroom.security")
public class SecurityConfig {

    @Value("${jwt.token.URL}")
    private String keyURL;
    @Bean
    public JwtAuthorizationHelper jwtAuthorizationHelper(){
        return new JwtAuthorizationHelper();
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .authorizeHttpRequests(auth ->{
                    auth
                        .requestMatchers("/", "/error").permitAll()
                        // 보안을 위해 k8s에 직접적으로 요청하는 API는 전부 deny
                        .requestMatchers("/core/**").denyAll()
                        .requestMatchers("/pod/**").hasAuthority("ROLE_Student")
                        .requestMatchers("/pod/{id}").access(new WebExpressionAuthorizationManager("hasAuthority('ID_'+#id)"))
                        .requestMatchers("/**").hasAuthority("ROLE_Professor")
                        .anyRequest().authenticated(); //
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
                .build();
    }

    @Bean
    public JwtAuthorizationConverter jwtAuthorizationConverter(){
        return new JwtAuthorizationConverter();
    }

}



