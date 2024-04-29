package com.devlatte.devroom.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthorizationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt token) {
        String email = token.getClaims().get("email").toString();
        List<String> roles = (List<String>)token.getClaims().get("cognito:groups");

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        log.info("HELLO!");
        log.info(email);
        roles.forEach(role -> log.info(role));


        return new JwtAuthenticationToken(token, authorities);
    }
}
