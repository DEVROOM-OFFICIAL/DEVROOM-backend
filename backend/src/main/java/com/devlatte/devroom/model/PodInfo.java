package com.devlatte.devroom.model;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.Data;

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
        this.creationTimestamp = pod.getMetadata().getCreationTimestamp().toString();
        this.status = pod.getStatus().getPhase(); // 또는 다른 상태 정보를 가져올 수 있습니다.
    }
}