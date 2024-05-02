package com.devlatte.devroom.k8s.controller.core;

import com.devlatte.devroom.k8s.api.core.PVApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.gson.JsonObject;

@RestController
@RequiredArgsConstructor
public class PVController extends K8sControllerBase{

    @Value("${config.kubernetes.nodeName}")
    private String nodeName;

    private final PVApi pvApi;


    @GetMapping(value = "/core/pv/all", produces = "application/json")
    public ResponseEntity<String> getPVAll() {
        String jsonData = pvApi.getInfo("all", null);
        return handleResponse(jsonData);
    }

    @GetMapping(value = "/core/pv/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getPVByLabel(@PathVariable String label, @PathVariable String value) {
        String jsonData = pvApi.getInfo(label, value);
        return handleResponse(jsonData);
    }

    @PostMapping(value = "/core/pv/create", produces = "application/json")
    public ResponseEntity<String> createPV(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String pvName = jsonObject.get("pvName").getAsString();
        String pvCapacity = jsonObject.get("pvCapacity").getAsString();
        return handleResponse(pvApi.createPV(pvName, pvCapacity, nodeName));
    }

    @PostMapping(value = "/core/pv/delete", produces = "application/json")
    public ResponseEntity<String> deletePV(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String pvName = jsonObject.get("pvName").getAsString();
        return handleResponse(pvApi.deletePV(pvName));
    }
}