package com.devlatte.devroom.k8s.controller;

import com.devlatte.devroom.k8s.api.*;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ServiceController extends K8sControllerBase{

    private final ServiceApi serviceApi;

    @GetMapping(value = "/service/all", produces = "application/json")
    public ResponseEntity<String> getServiceAll()  {
        return handleResponse(serviceApi.getInfo("all", null));
    }
    @GetMapping(value = "/service/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getServiceByLabel(@PathVariable String label, @PathVariable String value) {
        return handleResponse(serviceApi.getInfo(label, value));
    }
    @PostMapping(value = "/service/create", produces = "application/json")
    public ResponseEntity<String> createService(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String serviceName = jsonObject.get("serviceName").getAsString();
        String selector = jsonObject.get("selector").getAsString();
        String exPort = jsonObject.get("exPort").getAsString();
        String inPort = jsonObject.get("inPort").getAsString();
        Map<String, String> labels = response2Map(jsonObject, "labels");
        return handleResponse(serviceApi.createService(serviceName, selector, exPort, inPort, labels));
    }

    @PostMapping(value = "/service/delete", produces = "application/json")
    public ResponseEntity<String> deleteService(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String serviceName = jsonObject.get("serviceName").getAsString();
        return handleResponse(serviceApi.deleteService(serviceName));
    }
}