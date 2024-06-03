package com.devlatte.devroom.k8s.model;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PodInfo {
    private String name;
    private String ip;
    private Map<String, String> labels;
    private String creationTimestamp;
    private String status;

    public PodInfo(Pod pod) {
        this.name = pod.getMetadata().getName();
        this.ip = pod.getStatus().getPodIP();
        this.labels = pod.getMetadata().getLabels();
        this.creationTimestamp = pod.getMetadata().getCreationTimestamp();
        this.status = pod.getStatus().getPhase();
    }
}