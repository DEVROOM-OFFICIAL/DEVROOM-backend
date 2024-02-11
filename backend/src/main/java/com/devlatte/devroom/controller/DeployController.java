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
public class DeployController {
    private final GetInfoApi getInfoApi;

    @GetMapping(value = "/deploy/all", produces = "application/json")
    public ResponseEntity<String> getDeploymentsAll() {
        String jsonData = getInfoApi.getDeploy("all", null);
        return ResponseEntity.status(HttpStatus.OK).body(jsonData);
    }

    @GetMapping(value = "/deploy/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getDeploymentsByLabel(@PathVariable String label, @PathVariable String value) {
        String jsonData = getInfoApi.getDeploy(label, value);
        return ResponseEntity.status(HttpStatus.OK).body(jsonData);
    }
}
