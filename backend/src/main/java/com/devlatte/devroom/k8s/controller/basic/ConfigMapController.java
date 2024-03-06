package com.devlatte.devroom.k8s.controller.basic;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ConfigMapController extends K8sControllerBase{
    private final com.devlatte.devroom.k8s.api.basic.ConfigMapApi ConfigMapApi;

    @GetMapping(value = "/configmap/all", produces = "application/json")
    public ResponseEntity<String> getConfigMapAll() {
        return handleResponse(ConfigMapApi.getInfo("all", null));
    }

    @GetMapping(value = "/configmap/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getConfigMapByLabel(@PathVariable String label, @PathVariable String value) {
        return handleResponse(ConfigMapApi.getInfo(label, value));
    }
    @PostMapping(value = "/configmap/create", produces = "application/json")
    public ResponseEntity<String> createConfigMap(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String configMapName = jsonObject.get("configMapName").getAsString();
        Map<String, String> labels = response2Map(jsonObject, "labels");
        Map<String, String> data = response2Map(jsonObject, "data");
        return handleResponse(ConfigMapApi.createConfigMap(configMapName, labels, data));
    }

    @PostMapping(value = "/configmap/delete", produces = "application/json")
    public ResponseEntity<String> deleteConfigMap(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String configMapName = jsonObject.get("configMapName").getAsString();
        return handleResponse(ConfigMapApi.deleteConfigMap(configMapName));
    }
}
