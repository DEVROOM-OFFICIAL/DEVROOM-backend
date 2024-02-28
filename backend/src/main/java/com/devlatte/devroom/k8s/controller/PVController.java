package com.devlatte.devroom.k8s.controller;

import com.devlatte.devroom.k8s.api.PVApi;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequiredArgsConstructor
public class PVController extends K8sControllerBase{

    @Value("${config.kubernetes.nodeName}")
    private String nodeName;

    private final PVApi pvApi;


    @GetMapping(value = "/pv/all", produces = "application/json")
    public ResponseEntity<String> getPVAll() {
        String jsonData = pvApi.getInfo("all", null);
        return handleResponse(jsonData);
    }

    @GetMapping(value = "/pv/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getPVByLabel(@PathVariable String label, @PathVariable String value) {
        String jsonData = pvApi.getInfo(label, value);
        return handleResponse(jsonData);
    }

    @PostMapping(value = "/pv/create", produces = "application/json")
    public ResponseEntity<String> createPV(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String pvName = jsonObject.get("pvName").getAsString();
        String pvCapacity = jsonObject.get("pvCapacity").getAsString();
        return handleResponse(pvApi.createPV(pvName, pvCapacity, nodeName));
    }

    @PostMapping(value = "/pv/delete", produces = "application/json")
    public ResponseEntity<String> deletePV(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String pvName = jsonObject.get("pvName").getAsString();
        return handleResponse(pvApi.deletePV(pvName));
    }
}