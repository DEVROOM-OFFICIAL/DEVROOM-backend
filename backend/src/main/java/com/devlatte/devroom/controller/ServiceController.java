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
public class ServiceController {
    private final GetInfoApi getInfoApi;

    @GetMapping(value = "/service/all", produces = "application/json")
    public ResponseEntity<String> getServicesAll() {
        String jsonData = getInfoApi.getService("all", null);
        return ResponseEntity.status(HttpStatus.OK).body(jsonData);
    }

    @GetMapping(value = "/service/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getServicesByLabel(@PathVariable String label, @PathVariable String value) {
        String jsonData = getInfoApi.getService(label, value);
        return ResponseEntity.status(HttpStatus.OK).body(jsonData);
    }
}