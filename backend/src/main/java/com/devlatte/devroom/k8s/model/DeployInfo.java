package com.devlatte.devroom.k8s.model;

import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import lombok.Data;

import java.util.HashMap;
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
    private String pvClaim;
    private String configMapName;
    private Map<String, Map<String, String>> volumeMounts;
    private String cpuLimit;
    private String cpuRequest;
    private String memLimit; // Add RAM limit field
    private String memRequest; // Add RAM request field
    private List<String> command;


    public DeployInfo(Deployment deployment) {
        this.name = deployment.getMetadata().getName();
        this.replicas = deployment.getSpec().getReplicas();
        this.availableReplicas = deployment.getStatus().getAvailableReplicas() != null ? deployment.getStatus().getAvailableReplicas() : 0;
        this.unavailableReplicas = deployment.getStatus().getUnavailableReplicas() != null ? deployment.getStatus().getUnavailableReplicas() : 0;
        this.labels = deployment.getMetadata().getLabels();
        this.creationTimestamp = deployment.getMetadata().getCreationTimestamp().toString();

        this.pvClaim = deployment.getSpec().getTemplate().getSpec().getVolumes()
                .stream()
                .filter(volume -> volume.getPersistentVolumeClaim() != null)
                .map(volume -> volume.getPersistentVolumeClaim().getClaimName())
                .findFirst()
                .orElse(null);

        this.configMapName = deployment.getSpec().getTemplate().getSpec().getVolumes()
                .stream()
                .filter(volume -> volume.getConfigMap() != null)
                .map(volume -> volume.getConfigMap().getName())
                .findFirst()
                .orElse(null);


        this.volumeMounts = new HashMap<>();

        deployment.getSpec().getTemplate().getSpec().getContainers().forEach(container -> {
            container.getVolumeMounts().stream()
                    .forEach(volumeMount -> {
                        Map<String, String> mountInfo = new HashMap<>();
                        if (volumeMount.getSubPath() != null){
                            mountInfo.put("subpath", volumeMount.getSubPath());
                        }
                        mountInfo.put("mountpath", volumeMount.getMountPath());
                        volumeMounts.put(volumeMount.getName(), mountInfo);
                    });
        });

        this.cpuLimit = deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getLimits().containsKey("cpu") ?
                deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getLimits().get("cpu").getAmount() : "none";

        this.cpuRequest = deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getRequests().containsKey("cpu") ?
                deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getRequests().get("cpu").getAmount() : "none";

        this.memLimit = deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getLimits().containsKey("memory") ?
                deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getLimits().get("memory").getAmount() : "none";

        this.memRequest = deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getRequests().containsKey("memory") ?
                deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getResources().getRequests().get("memory").getAmount() : "none";

        this.command = deployment.getSpec().getTemplate().getSpec().getContainers().getFirst().getCommand();
    }
}