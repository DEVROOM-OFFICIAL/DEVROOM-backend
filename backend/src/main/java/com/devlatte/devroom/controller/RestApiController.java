package com.devlatte.devroom.controller;

import com.devlatte.devroom.service.RestApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RestApiController {
    private final RestApiService restApiService;

    @GetMapping("/pods")
    public String getPods(){
        return restApiService.getResource();
    }
}
