package com.devlatte.devroom.k8s.controller.core;

import com.devlatte.devroom.k8s.api.core.PVClaimApi;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PVClaimController extends K8sControllerBase{

    @Value("${config.kubernetes.nodeName}")
    private String nodeName;

    private final PVClaimApi pvClaimApi;

    @GetMapping(value = "/core/pv-claim/all", produces = "application/json")
    public ResponseEntity<String> getPVClaimAll() {
        return handleResponse(pvClaimApi.getInfo("all", null));
    }

    @GetMapping(value = "/core/pv-claim/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getPVClaimByLabel(@PathVariable String label, @PathVariable String value) {
        return handleResponse(pvClaimApi.getInfo(label, value));
    }

    @PostMapping(value = "/core/pv-claim/create", produces = "application/json")
    public ResponseEntity<String> createPVClaim(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String pvClaimName = jsonObject.get("pvClaimName").getAsString();
        String pvCapacity = jsonObject.get("pvCapacity").getAsString();
        return handleResponse(pvClaimApi.createPVClaim(pvClaimName, pvCapacity));
    }

    @PostMapping(value = "/core/pv-claim/delete", produces = "application/json")
    public ResponseEntity<String> deletePV(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String pvClaimName = jsonObject.get("pvClaimName").getAsString();
        return handleResponse(pvClaimApi.deletePVClaim(pvClaimName));
    }
}