package com.devlatte.devroom.k8s.controller.basic;


import com.devlatte.devroom.k8s.api.basic.ExecApi;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ExecController extends K8sControllerBase{

    private final ExecApi execApi;
    @PostMapping(value = "/cmd/exec", produces = "application/json")
    public ResponseEntity<String> runCmd(@RequestBody String requestBody) throws IOException, InterruptedException {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
        JsonArray commandArray = jsonObject.getAsJsonArray("command");
        String labelKey = jsonObject.get("labelKey").getAsString();
        String labelValue = jsonObject.get("labelValue").getAsString();
        String[] command = new String[commandArray.size()];
        for (int i = 0; i < commandArray.size(); i++) {
            command[i] = commandArray.get(i).getAsString();
        }

        return handleResponse(execApi.run(labelKey, labelValue, command));
    }

}
