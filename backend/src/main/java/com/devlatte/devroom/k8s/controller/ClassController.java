package com.devlatte.devroom.k8s.controller;

import com.devlatte.devroom.k8s.api.ClassApi;
import com.devlatte.devroom.k8s.controller.basic.K8sControllerBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ClassController extends K8sControllerBase {

    private final ClassApi classApi;

    @PostMapping(value = "/class/create", produces = "application/json")
    public ResponseEntity<String> createClass(@RequestBody String requestBody) throws IOException, InterruptedException {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

        String className = jsonObject.get("className").getAsString();
        String customScript = jsonObject.get("customScript").getAsString();
        Map<String, String> options = response2Map(jsonObject, "options");
        Map<String, String> studentId2Port = response2Map(jsonObject,"studentId2Port");
        JsonArray commandArray = jsonObject.getAsJsonArray("command");
        String[] command = new String[commandArray.size()];
        for (int i = 0; i < commandArray.size(); i++) command[i] = commandArray.get(i).getAsString();

        return handleResponse(classApi.create(className, studentId2Port, options, command, customScript));
    }

    @PostMapping(value = "/class/delete", produces = "application/json")
    public ResponseEntity<String> deleteClass(@RequestBody String requestBody) throws IOException, InterruptedException {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

        String className = jsonObject.get("className").getAsString();
        JsonArray studentIdsArray = jsonObject.getAsJsonArray("studentIds");
        List<String> studentIds = new ArrayList<>();
        for (JsonElement element : studentIdsArray) studentIds.add(element.getAsString());

        return handleResponse(classApi.delete(className, studentIds));
    }

}
