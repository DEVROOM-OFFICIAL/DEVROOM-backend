package com.devlatte.devroom.k8s.controller.user;

import com.devlatte.devroom.k8s.api.core.PodApi;
import com.devlatte.devroom.k8s.api.user.ClassApi;
import com.devlatte.devroom.k8s.controller.core.K8sControllerBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfessorController extends K8sControllerBase {

    private final ClassApi classApi;
    private final PodApi podApi;

    @PostMapping(value = "/class/{id}/create", produces = "application/json")
    public ResponseEntity<String> createClass(@RequestBody String requestBody, @PathVariable("id") String professorId) throws IOException, InterruptedException {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

        String className = jsonObject.get("className").getAsString();
        String customScript = jsonObject.get("customScript").getAsString();
        Map<String, String> options = response2Map(jsonObject, "options");
        JsonArray studentIdsArray = jsonObject.getAsJsonArray("studentIds");
        List<String> studentIds = new ArrayList<>();
        for (JsonElement element : studentIdsArray) studentIds.add(element.getAsString());
        JsonArray commandArray = jsonObject.getAsJsonArray("command");
        String[] command = new String[commandArray.size()];
        for (int i = 0; i < commandArray.size(); i++) command[i] = commandArray.get(i).getAsString();

        return handleResponse(classApi.create(className, professorId, studentIds, options, command, customScript));
    }

    @PostMapping(value = "/class/{id}/delete", produces = "application/json")
    public ResponseEntity<String> deleteClass(@RequestBody String requestBody, @PathVariable("id") String professorId) throws IOException, InterruptedException {
        JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

        String className = jsonObject.get("className").getAsString();
        JsonArray studentIdsArray = jsonObject.getAsJsonArray("studentIds");
        String studentId  = jsonObject.get("studentId").getAsString();

        return handleResponse(classApi.delete(className, studentId, professorId));
    }
    @GetMapping(value = "/class/{id}/pod", produces = "application/json")
    public ResponseEntity<String> getPodByLabel(@PathVariable String id) {
        return handleResponse(podApi.getInfo("professor_id", "id-"+id));
    }

}
