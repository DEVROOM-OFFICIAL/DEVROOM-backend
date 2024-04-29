package com.devlatte.devroom.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

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
                        .requestMatchers("/pod/**", "/service/**").hasAuthority("ROLE_Student")
                        .requestMatchers("/**").hasAuthority("ROLE_Professor")
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
