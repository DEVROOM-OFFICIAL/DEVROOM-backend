package com.devlatte.devroom.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

@Slf4j
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    @Value("${jwt.token.URL}")
    private String keyURL;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .authorizeHttpRequests(auth ->{auth
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers("/**").hasAuthority("ROLE_Professor")
                        .requestMatchers("/pod/**", "/service/**").hasAuthority("ROLE_Student")
                        // 본인의 id의 주소로만 접근이 가능하게 해야할 것 같습니다. 검색해본 결과 아래 같은 느낌인것 같지만, 정확하게 구현이 필요합니다.
                        // .requestMatchers("/pod/student_id/{id}", "/service/**").access()
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
                .build();
    }

    @Bean
    public JwtAuthorizationConverter jwtAuthorizationConverter(){
        return new JwtAuthorizationConverter();
    }

}


