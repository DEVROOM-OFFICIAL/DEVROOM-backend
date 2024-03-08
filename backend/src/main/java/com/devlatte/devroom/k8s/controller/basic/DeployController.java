package com.devlatte.devroom.k8s.controller.basic;

import com.devlatte.devroom.k8s.api.basic.DeployApi;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        String cpuReq = jsonObject.get("cpuReq").getAsString();
        String cpuLimit = jsonObject.get("cpuLimit").getAsString();
        String memReq = jsonObject.get("memReq").getAsString();
        String memLimit = jsonObject.get("memLimit").getAsString();

        Map<String, String> labels = response2Map(jsonObject, "labels");

        JsonObject volumesJson = jsonObject.getAsJsonObject("volumes");
        Map<String, Map<String, String>> volumes = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : volumesJson.entrySet()) {
            String volumeName = entry.getKey();
            JsonObject volumeDetailsJson = entry.getValue().getAsJsonObject();

            Map<String, String> volumeDetails = new HashMap<>();
            volumeDetails.put("pvPath", volumeDetailsJson.get("pvPath").getAsString());
            volumeDetails.put("mountPath", volumeDetailsJson.get("mountPath").getAsString());
            volumeDetails.put("isReadOnly", volumeDetailsJson.get("isReadOnly").getAsString());
            volumes.put(volumeName, volumeDetails);
        }

        JsonArray commandArray = jsonObject.getAsJsonArray("command");
        String[] command = new String[commandArray.size()];
        for (int i = 0; i < commandArray.size(); i++) {
            command[i] = commandArray.get(i).getAsString();
        }

        return handleResponse(deployApi.createDeploy(
                deployName, hostName, image, selector,
                cpuReq, cpuLimit, memReq, memLimit, labels, volumes, command));
    }

    @PostMapping(value = "/deploy/delete", produces = "application/json")
    public ResponseEntity<String> deleteDeploy(@RequestBody String requestBody) {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        String deployName = jsonObject.get("deployName").getAsString();
        return handleResponse(deployApi.deleteDeploy(deployName));
    }
}
