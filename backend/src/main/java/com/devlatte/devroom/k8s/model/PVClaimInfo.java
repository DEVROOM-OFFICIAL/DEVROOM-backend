package com.devlatte.devroom.k8s.model;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import lombok.Data;

import java.util.Map;

@Data
public class PVClaimInfo {
    private String name;
    private String capacity;
    private String accessModes;
    private String storageClassName;
    private Map<String, String> labels;
    private String creationTimestamp;

    public PVClaimInfo(PersistentVolumeClaim persistentVolumeClaim) {
        this.name = persistentVolumeClaim.getMetadata().getName();
        this.capacity = persistentVolumeClaim.getSpec().getResources().getRequests().get("storage").toString();
        this.accessModes = String.join(",", persistentVolumeClaim.getSpec().getAccessModes());
        this.storageClassName = persistentVolumeClaim.getSpec().getStorageClassName();
        this.labels = persistentVolumeClaim.getMetadata().getLabels();
        this.creationTimestamp = persistentVolumeClaim.getMetadata().getCreationTimestamp();
    }
}