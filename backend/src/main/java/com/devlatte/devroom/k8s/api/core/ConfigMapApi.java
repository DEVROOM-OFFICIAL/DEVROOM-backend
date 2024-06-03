package com.devlatte.devroom.k8s.api.core;


import com.devlatte.devroom.k8s.model.ConfigMapInfo;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConfigMapApi extends K8sApiBase { // Rename class to ConfigMapApi

    public ConfigMapApi(@Value("${config.kubernetes.url}") String apiServer,
                        @Value("${config.kubernetes.token}") String apiToken) {
        super(apiServer, apiToken);
    }

    public String getInfo(String label, String value) { // Rename method to getInfo
        try {
            List<ConfigMapInfo> configMaps;
            if ("all".equals(label)) {
                configMaps = k8s.configMaps().inNamespace("default").list().getItems()
                        .stream()
                        .map(ConfigMapInfo::new) // ConfigMapInfo 객체로 매핑
                        .collect(Collectors.toList());
            } else {
                configMaps = k8s.configMaps().inNamespace("default").withLabel(label, value).list().getItems()
                        .stream()
                        .map(ConfigMapInfo::new) // ConfigMapInfo 객체로 매핑
                        .collect(Collectors.toList());
            }
            return gson.toJson(configMaps);
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String createConfigMap(String configMapName, Map<String, String> labels,  Map<String, String> data) {
        try {

            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(configMapName);
            metadata.setLabels(labels);

            ConfigMap configMap = new ConfigMap();
            configMap.setMetadata(metadata);
            configMap.setData(data);

            k8s.configMaps().inNamespace("default").resource(configMap).create();
            ConfigMapInfo configMapInfo = new ConfigMapInfo(k8s.configMaps().inNamespace("default").withName(configMapName).get());
            return gson.toJson(configMapInfo);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }
    public String deleteConfigMap(String configMapName) {
        try {
            if (k8s.configMaps().inNamespace("default").withName(configMapName).get() != null) {
                k8s.configMaps().inNamespace("default").withName(configMapName).withGracePeriod(0).delete();
                HashMap<String, String> successMap = new HashMap<>();
                successMap.put("success", "ConfigMap deleted successfully");
                return gson.toJson(successMap);
            } else {
                HashMap<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "ConfigMap doesn't exist");
                return gson.toJson(errorMap);
            }
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }
}
