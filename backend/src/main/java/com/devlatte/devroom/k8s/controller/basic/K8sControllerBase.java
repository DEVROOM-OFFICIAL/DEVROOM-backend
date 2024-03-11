package com.devlatte.devroom.k8s.controller.basic;

import com.devlatte.devroom.k8s.api.basic.K8sApiBase;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public abstract class K8sControllerBase {

    protected final Gson gson = new Gson();
    protected final Logger logger = LoggerFactory.getLogger(K8sApiBase.class);
    protected ResponseEntity<String> handleResponse(String jsonData) {
        try {
            // Array로 받아야하는 경우 ex) pod의 목록
            JsonArray jsonArray = JsonParser.parseString(jsonData).getAsJsonArray();
            return ResponseEntity.status(HttpStatus.OK).body(jsonData);
        } catch (IllegalStateException e) {
            // Array가 아닌 단일 json인 경우
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            // error가 있는 경우
            if (jsonObject.has("error")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonData);
            } else { // 정상인 경우
                return ResponseEntity.status(HttpStatus.OK).body(jsonData);
            }
        }
    }

    protected Map<String, String> response2Map(JsonObject jsonObject, String keyName){
        return gson.fromJson(jsonObject.get(keyName), new TypeToken<Map<String, String>>(){}.getType());
    }
}
