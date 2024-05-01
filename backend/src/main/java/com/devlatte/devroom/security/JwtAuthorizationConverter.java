package com.devlatte.devroom.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthorizationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt token) {

        String email = token.getClaims().get("email").toString();
        String custom_role = token.getClaims().get("custom:role").toString();
        String studentId = token.getClaims().get("custom:student_id").toString();
        List<String> roles = new ArrayList<>();
        roles.add(custom_role);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        log.info("HELLO!");
        log.info(email);
        log.info(studentId);
        roles.forEach(role -> log.info(role));

        return new JwtAuthenticationToken(token, authorities);
    }

}
