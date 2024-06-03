package com.devlatte.devroom.k8s.controller.core;

import com.devlatte.devroom.k8s.utils.*;
import com.devlatte.devroom.k8s.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class PortController extends K8sControllerBase{

    private final PortFind portFind;

    @GetMapping(value = "/core/port/find", produces = "application/json")
    public ResponseEntity<String> getPort()  {
        try{
            String port = portFind.get(false);
            HashMap<String, String> successMap = new HashMap<>();
            successMap.put("port", port);
            return handleResponse(gson.toJson(successMap));

        } catch (NoAvailablePortException e){
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return handleResponse(gson.toJson(errorMap));
        }
    }
}