package com.devlatte.devroom.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Value("${jwt.token.key}")
    private String secretKey;
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

            log.debug("member_id = {}", memberId);
            log.debug("member_pw = {}", memberPw);

            Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, memberPw);

            log.debug("authentication token made : {}", authentication);
            return getAuthenticationManager().authenticate(authentication);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        long expirationTime = 864_000_000; // 10 days

        String jwtToken = Jwts.builder()
                .setSubject(authResult.getName()) // 사용자 이름 또는 ID 설정
                .claim("authorities", authResult.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList())) // 사용자 권한 추가
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes())
                .compact();

        System.out.println("member name = " + authResult.getName());
        System.out.println("authorities = " +  authResult.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        System.out.println("generated jwt token = " + jwtToken);

        response.addHeader("Authorization", "Bearer " + jwtToken); // 응답 헤더에 토큰 추가

        /*
        String redirectUrl = authResult.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_STUDENT")) ? "/student" : "/professor";

        response.sendRedirect(request.getContextPath() + redirectUrl);
        */
        //chain.doFilter(request, response);
    }
}
