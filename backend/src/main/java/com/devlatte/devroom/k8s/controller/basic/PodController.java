package com.devlatte.devroom.k8s.controller.basic;

import com.devlatte.devroom.k8s.api.basic.PodApi;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PodController extends K8sControllerBase{

    private final PodApi podApi;

    @GetMapping(value = "/pod/all", produces = "application/json")
    public ResponseEntity<String> getPodAll() {
        return handleResponse(podApi.getInfo("all", null));
    }
    @GetMapping(value = "/pod/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getPodByLabel(@PathVariable String label, @PathVariable String value) {
        return handleResponse(podApi.getInfo(label, value));
    }

    @PostMapping(value = "/pod/create", produces = "application/json")
    public ResponseEntity<String> createPod(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String podName = jsonObject.get("podName").getAsString();
        String containerImage = jsonObject.get("containerImage").getAsString();
        return handleResponse(podApi.createPod(podName, containerImage));
    }

    @PostMapping(value = "/pod/delete", produces = "application/json")
    public ResponseEntity<String> deletePod(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String podName = jsonObject.get("podName").getAsString();
        return handleResponse(podApi.deletePod(podName));
    }
}