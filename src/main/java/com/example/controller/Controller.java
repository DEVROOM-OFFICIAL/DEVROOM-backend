package com.example.controller;

import com.example.RestApiService;
import com.example.dto.PodDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.RestApiUtil.RESOURCE_TYPE_POD;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final RestApiService restApiService;

    @GetMapping("/pods")
    public List<PodDto> getPods(){
        return restApiService.getResource(RESOURCE_TYPE_POD);
    }
}
