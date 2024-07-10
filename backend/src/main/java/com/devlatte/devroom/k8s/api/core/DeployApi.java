package com.devlatte.devroom.k8s.api.core;

import io.fabric8.kubernetes.api.model.*;
import com.devlatte.devroom.k8s.model.DeployInfo;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class DeployApi extends K8sApiBase {

    public DeployApi(@Value("${config.kubernetes.url}") String apiServer,
                     @Value("${config.kubernetes.token}") String apiToken) {
        super(apiServer, apiToken);
    }


    public String getInfo(String label, String value) {
        try {
            List<DeployInfo> deployments;
            if ("all".equals(label)) {
                deployments = k8s.apps().deployments().inNamespace("default").list().getItems()
                        .stream()
                        .map(DeployInfo::new)
                        .collect(Collectors.toList());
            } else {
                deployments = k8s.apps().deployments().inNamespace("default").withLabel(label, value).list().getItems()
                        .stream()
                        .map(DeployInfo::new)
                        .collect(Collectors.toList());
            }
            return gson.toJson(deployments);
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String createDeploy(String deployName,
                               String hostName,
                               String image,
                               String selector,
                               String cpuReq,
                               String cpuLimit,
                               String memReq,
                               String memLimit,
                               Map<String, String> labels,
                               Map<String, Map<String, String>> volumes,
                               String[] command
    ) {


        List<VolumeMount> volumeMounts = new ArrayList<>();
        List<Volume> volumesList = new ArrayList<>();

        // 컨피그맵 등록
        VolumeMount configMount = new VolumeMountBuilder()
                .withName("config")
                .withMountPath("/app/config")
                .withReadOnly(true)
                .build();
        volumeMounts.add(configMount);

        Volume config = new VolumeBuilder()
                .withName("config")
                .withConfigMap(new ConfigMapVolumeSourceBuilder()
                        .withName(deployName + "-config")
                        .build())
                .build();
        volumesList.add(config);

        // 기타 볼륨 등록
        for (Map.Entry<String, Map<String, String>> entry : volumes.entrySet()) {
            String volumeName = entry.getKey();
            Map<String, String> volumeDetails = entry.getValue();
            String hostPath = volumeDetails.get("hostPath");
            String mountPath = volumeDetails.get("mountPath");
            boolean isReadOnly = Boolean.parseBoolean(volumeDetails.get("isReadOnly"));

            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withName(volumeName)
                    .withMountPath(mountPath)
//                    .withSubPath(hostPath)
                    .withReadOnly(isReadOnly)
                    .build();
            volumeMounts.add(volumeMount);

            Volume volume = new VolumeBuilder()
                    .withName(volumeName)
                    .withHostPath(new HostPathVolumeSourceBuilder()
                            .withPath("/"+hostPath)
                            .withType("Directory")  // 필요에 따라 "Directory", "File" 등으로 설정 가능
                            .build())
                    .build();
            volumesList.add(volume);

        }


        try {
            Deployment deploy = new DeploymentBuilder()
                .withNewMetadata()
                .withName(deployName)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withSelector(new LabelSelectorBuilder()
                    .addToMatchLabels("app", deployName)
                    .build())
                .withNewTemplate()
                .withNewMetadata()
                    .withLabels(labels)
                    .addToLabels("app", deployName)
                .endMetadata()
                .withSpec(new PodSpecBuilder()
                    .withHostname(hostName)
                    .addToContainers(new ContainerBuilder()
                        .withName(deployName + "-container")
                        .withImage(image)
                        .withResources(new ResourceRequirementsBuilder()
                        .addToRequests("cpu", new Quantity(cpuReq))
//                        .addToLimits("cpu", new Quantity(cpuLimit))
                        .addToRequests("memory", new Quantity(memReq)) // RAM request
                        .addToLimits("memory", new Quantity(memLimit))     // RAM limit
                        .build())
                        .withVolumeMounts(volumeMounts)
                        .withCommand(command)
                        .build())
                    .withVolumes(volumesList)
                    .build())
                .endTemplate()
                .endSpec()
                .build();

            k8s.apps().deployments().inNamespace("default").resource(deploy).create();
            DeployInfo deployInfo = new DeployInfo(k8s.apps().deployments().inNamespace("default").withName(deployName).get());
            return gson.toJson(deployInfo);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String deleteDeploy(String deployName) {
        try {
            if (k8s.apps().deployments().inNamespace("default").withName(deployName).get() != null) {

                // List<Pod> pods = k8s.pods().inNamespace("default")
                //         .withLabel("app", deployName) // Assumes pods have a label 'app' with the deployment name
                //         .list()
                //         .getItems();
                // for (Pod pod : pods) k8s.pods().inNamespace("default").withName(pod.getMetadata().getName()).delete();

                k8s.apps().deployments().inNamespace("default").withName(deployName).delete();
                HashMap<String, String> successMap = new HashMap<>();

                // for (Pod pod : pods) k8s.pods().inNamespace("default").withName(pod.getMetadata().getName()).delete();

                successMap.put("success", "Deploy deleted successfully");
                return gson.toJson(successMap);
            } else {
                HashMap<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Deploy doesn't exist");
                return gson.toJson(errorMap);
            }
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }
}