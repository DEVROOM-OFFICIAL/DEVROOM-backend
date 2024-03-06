package com.devlatte.devroom.k8s.controller.basic;

import com.devlatte.devroom.k8s.api.basic.DeployApi;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DeployController extends K8sControllerBase{
    private final DeployApi deployApi;

    @GetMapping(value = "/deploy/all", produces = "application/json")
    public ResponseEntity<String> getDeploymentsAll() {
        return handleResponse(deployApi.getInfo("all", null));
    }

    @GetMapping(value = "/deploy/{label}/{value}", produces = "application/json")
    public ResponseEntity<String> getDeploymentsByLabel(@PathVariable String label, @PathVariable String value) {
        return handleResponse(deployApi.getInfo(label, value));
    }
    @PostMapping(value = "/deploy/create", produces = "application/json")
    public ResponseEntity<String> createDeploy(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String deployName = jsonObject.get("deployName").getAsString();
        String selector = jsonObject.get("selector").getAsString();
        String hostName = jsonObject.get("hostName").getAsString();
        String image = jsonObject.get("image").getAsString();
        String pvName = jsonObject.get("pvName").getAsString();
        String pvPath = jsonObject.get("pvPath").getAsString();
        String mountPath = jsonObject.get("mountPath").getAsString();
        JsonArray commandArray = jsonObject.getAsJsonArray("command");
        String[] command = new String[commandArray.size()];
        for (int i = 0; i < commandArray.size(); i++) {
            command[i] = commandArray.get(i).getAsString();
        }
        String cpuReq = jsonObject.get("cpuReq").getAsString();
        String cpuLimit = jsonObject.get("cpuLimit").getAsString();
        Map<String, String> labels = response2Map(jsonObject, "labels");

        return handleResponse(deployApi.createDeploy(
                deployName, hostName, image, pvName,
                pvPath, mountPath, selector, command,
                cpuReq, cpuLimit, labels ));
    }

    @PostMapping(value = "/deploy/delete", produces = "application/json")
    public ResponseEntity<String> deleteDeploy(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String deployName = jsonObject.get("deployName").getAsString();
        return handleResponse(deployApi.deleteDeploy(deployName));
    }
}
