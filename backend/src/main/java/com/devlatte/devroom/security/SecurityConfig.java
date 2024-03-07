package com.devlatte.devroom.security;

import com.devlatte.devroom.dto.ErrorResponse;
import com.devlatte.devroom.entity.MemberRole;
import com.devlatte.devroom.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Slf4j
@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final MemberService memberService;
    private final ObjectPostProcessor<Object> objectPostProcessor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers("/", "/login", "/error", "/register", "/login-proc").permitAll()
                                .requestMatchers("/student/**").hasRole(MemberRole.Student.name())
                                .requestMatchers("/professor/**").hasRole(MemberRole.Professor.name())
                                .anyRequest().authenticated()
                )
                // JWT 인증방식 채택, CSRF 및 httpBasic, formLogin 형태의 인증 disable
                .csrf((csrfConfig) ->
                        csrfConfig.disable()
                )
                .httpBasic((httpBasieConfig) ->
                        httpBasieConfig.disable()
                )
                .formLogin((formLoginConfig) ->
                        formLoginConfig.disable()
                )
                // JWT 인증 형식이므로, session은 stateless로 선언
                .sessionManagement((sessionConfig) ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilter(getJwtAuthenticationFilter())
                .logout((logout) ->
                        logout.logoutSuccessUrl("/")
                )
                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    //커스터마이징한 authenticationManager를 jwtauthenticationfilter의 기본 authenticationmanager로 설정함
    public JwtAuthenticationFilter getJwtAuthenticationFilter() throws Exception{
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter();

        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager(builder));

        return jwtAuthenticationFilter;
    }


    //authenticationManager의 설정 진행
    private AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
        return auth.build();
    }



    // AuthenticationEntryPoint : 인증 과정 중에 인증 실패 발생시 어떤 동작을 수행할 것인지 정의하는 컴포넌트
    private final AuthenticationEntryPoint authenticationEntryPoint = ((request, response, authException) -> {
        log.error("Not Authenticated Request", authException);
        log.error("Request Uri : {}", request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized Request");
        String responseBody = objectMapper.writeValueAsString(errorResponse);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    });

    private final AccessDeniedHandler accessDeniedHandler = ((request, response, authException) -> {
        log.error("Forbidden Request", authException);
        log.error("Request Uri : {}", request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, "Forbidden Request");
        String responseBody = objectMapper.writeValueAsString(errorResponse);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    });
}
