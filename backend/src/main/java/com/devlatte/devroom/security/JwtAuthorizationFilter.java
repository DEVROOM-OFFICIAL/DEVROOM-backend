package com.devlatte.devroom.security;

import com.devlatte.devroom.repository.MemberRepository;
import com.devlatte.devroom.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter{

    @Value("${jwt.token.key}")
    private String secretKey;
    private MemberService memberService;

    public JwtAuthorizationFilter
            (AuthenticationManager authenticationManager, MemberService memberService) {
        super(authenticationManager);
        this.memberService = memberService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try{

            String requestHeader = request.getHeader("Authentication");
            String jwtToken = null;

            if(requestHeader != null && requestHeader.startsWith("Bearer")){
                jwtToken = requestHeader.replace("Bearer ", "");

                if(jwtToken != null && tokenValidation(jwtToken)){
                    log.info("jwtToken = {}", jwtToken);
                    SecurityContextHolder.getContext().setAuthentication(getAuth(jwtToken));
                }
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        chain.doFilter(request, response);
    }

    private boolean tokenValidation(String jwtToken){
        try{
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwtToken).getBody()
                    .getExpiration();

            return expiration.after(new Date());
        } catch(Exception e){
            return false;
        }
    }

    private Authentication getAuth(String jwtToken){
        UserDetails userDetails =
                memberService.loadUserByUsername(getUsernameFromToken(jwtToken));

        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
    }

    private String getUsernameFromToken(String jwtToken){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(jwtToken).getBody()
                .getSubject();
    }

}
