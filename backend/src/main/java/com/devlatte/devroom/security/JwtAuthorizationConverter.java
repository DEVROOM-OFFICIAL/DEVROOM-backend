package com.devlatte.devroom.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Configuration
public class JwtAuthorizationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt token) {

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        String email = token.getClaims().get("email").toString();
        String custom_role = token.getClaims().get("custom:role").toString();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + custom_role));

        if (token.getClaims().containsKey("custom:student_id")) {
            String studentId = token.getClaims().get("custom:student_id").toString();
            authorities.add(new SimpleGrantedAuthority("ID_" + studentId));
            log.info(studentId);
        }

        log.info(email);
        log.info(custom_role);

        return new JwtAuthenticationToken(token, authorities);
    }
}
