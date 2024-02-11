package com.devlatte.devroom.controller;

import com.devlatte.devroom.api.GetInfoApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PodController {
    private final GetInfoApi getInfoApi;

    @GetMapping(value = "/pod/all", produces = "application/json")
    public ResponseEntity<String> getPodsAll() {
        String jsonData = getInfoApi.getPod("all", null);
        return ResponseEntity.status(HttpStatus.OK).body(jsonData);
    }
    @GetMapping(value = "/pod/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getPodsByLabel(@PathVariable String label, @PathVariable String value) {
        String jsonData = getInfoApi.getPod(label, value);
        return ResponseEntity.status(HttpStatus.OK).body(jsonData);
    }
}