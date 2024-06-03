package com.devlatte.devroom.k8s.model;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.OwnerReference;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ConfigMapInfo {
    private String name;
    private Map<String, String> data;
    private Map<String, String> labels;
    private String creationTimestamp;
    private String namespace;
    private Map<String, String> annotations;
    private String resourceVersion;
    private List<String> ownerReferences;

    public ConfigMapInfo(ConfigMap configMap) {
        this.name = configMap.getMetadata().getName();
        this.data = configMap.getData();
        this.labels = configMap.getMetadata().getLabels();
        this.creationTimestamp = configMap.getMetadata().getCreationTimestamp();
        this.namespace = configMap.getMetadata().getNamespace();
        this.annotations = configMap.getMetadata().getAnnotations();
        this.resourceVersion = configMap.getMetadata().getResourceVersion();
        this.ownerReferences = configMap.getMetadata().getOwnerReferences().stream()
                .map(OwnerReference::getName)
                .collect(Collectors.toList());
    }
}
