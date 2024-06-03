package com.devlatte.devroom.k8s.model;

import io.fabric8.kubernetes.api.model.PersistentVolume;
import lombok.Data;

import java.util.Map;

@Data
public class PVInfo {
    private String name;
    private String capacity;
    private String accessModes;
    private String storageClassName;
    private Map<String, String> labels;
    private String creationTimestamp;

    public PVInfo(PersistentVolume persistentVolume) {
        this.name = persistentVolume.getMetadata().getName();
        this.capacity = persistentVolume.getSpec().getCapacity().get("storage").toString();
        this.accessModes = String.join(",", persistentVolume.getSpec().getAccessModes());
        this.storageClassName = persistentVolume.getSpec().getStorageClassName();
        this.labels = persistentVolume.getMetadata().getLabels();
        this.creationTimestamp = persistentVolume.getMetadata().getCreationTimestamp();
    }
}