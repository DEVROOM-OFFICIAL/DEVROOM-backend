package com.devlatte.devroom.k8s.controller.user;

import com.devlatte.devroom.k8s.api.core.DeployApi;
import com.devlatte.devroom.k8s.api.core.PodApi;
import com.devlatte.devroom.k8s.api.core.ServiceApi;
import com.devlatte.devroom.k8s.controller.core.K8sControllerBase;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudentController extends K8sControllerBase {

    private final PodApi podApi;
    private final ServiceApi serviceApi;
    private final DeployApi deployApi;

    @GetMapping(value = "/pod/{id}", produces = "application/json")
    public ResponseEntity<String> getPodByLabel(@PathVariable String id) {
        return handleResponse(podApi.getInfo("student_id", "id-"+id));
    }

    @GetMapping(value = "/service/{id}", produces = "application/json")
    public ResponseEntity<String> getServiceByLabel(@PathVariable String id) {
        return handleResponse(serviceApi.getInfo("student_id", "id-"+id));
    }

    @GetMapping(value = "/deploy/{id}", produces = "application/json")
    public ResponseEntity<String> getDeploymentsByLabel( @PathVariable String id) {
        return handleResponse(deployApi.getInfo("student_id", "id-"+id));
    }

}
