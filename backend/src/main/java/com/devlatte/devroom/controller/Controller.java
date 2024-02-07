package com.devlatte.devroom.controller;

import com.devlatte.devroom.RestApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final RestApiService restApiService;

    @GetMapping("/pods")
    public String getPods(){
        return restApiService.getResource();
    }
}
