package com.devlatte.devroom.model;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import lombok.Data;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DeployInfo {
    private String name;
    private int replicas;
    private int availableReplicas;
    private int unavailableReplicas;
    private Map<String, String> labels;
    private String creationTimestamp;
    private List<String> podNames;

    public DeployInfo(Deployment deployment) {
        this.name = deployment.getMetadata().getName();
        this.replicas = deployment.getSpec().getReplicas();
        this.availableReplicas = deployment.getStatus().getAvailableReplicas() != null ? deployment.getStatus().getAvailableReplicas() : 0;
        this.unavailableReplicas = deployment.getStatus().getUnavailableReplicas() != null ? deployment.getStatus().getUnavailableReplicas() : 0;
        this.labels = deployment.getMetadata().getLabels();
        this.creationTimestamp = deployment.getMetadata().getCreationTimestamp().toString();
    }
}