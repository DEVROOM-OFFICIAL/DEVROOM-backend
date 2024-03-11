package com.devlatte.devroom.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    //try-catch?
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 요청의 입력 스트림으로부터 JSON을 읽어와 Map 객체로 변환
            Map<String, String> requestData = objectMapper.readValue(request.getInputStream(), Map.class);

            // Map에서 필요한 데이터 추출
            String memberId = requestData.get("member_id");
            String memberPw = requestData.get("member_pw");


            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(memberId, memberPw);
            Authentication authentication = getAuthenticationManager().authenticate(authToken);


            UserDetails principal = (UserDetails) authentication.getPrincipal();

            log.debug("username : {}", principal.getUsername());
            log.debug("authority : {}", principal.getAuthorities());

            return authentication;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 인증 성공했을 시 실행되는 경우
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
    }
}
