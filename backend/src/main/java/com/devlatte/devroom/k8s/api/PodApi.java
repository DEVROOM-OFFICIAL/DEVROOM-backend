package com.devlatte.devroom.k8s.api;

import com.devlatte.devroom.k8s.model.PodInfo;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PodApi extends K8sApiBase { // Rename class to PodApi

    public PodApi(@Value("${config.kubernetes.url}") String apiServer,
                  @Value("${config.kubernetes.token}") String apiToken) {
        super(apiServer, apiToken);
    }

    public String getInfo(String label, String value) { // Rename method to getInfo
        try {
            List<PodInfo> podNames;
            if ("all".equals(label)) {
                podNames = k8s.pods().inNamespace("default").list().getItems() // Change services() to pods()
                        .stream()
                        .map(PodInfo::new) // PodInfo 객체로 매핑
                        .collect(Collectors.toList());
            } else {
                podNames = k8s.pods().inNamespace("default").withLabel(label, value).list().getItems() // Change services() to pods()
                        .stream()
                        .map(PodInfo::new) // PodInfo 객체로 매핑
                        .collect(Collectors.toList());
            }
            return gson.toJson(podNames);
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            return "Error occurred while communicating with Kubernetes";
        }
    }
    public String createPod(String podName, String containerImage) {
        try {
            Container container = new ContainerBuilder()
                    .withName(podName+"-container")
                    .withImage(containerImage)
                    .build();

            Pod pod = new PodBuilder()
                    .withNewMetadata()
                    .withName(podName)
                    .endMetadata()
                    .withNewSpec()
                    .withContainers(container)
                    .endSpec()
                    .build();

            k8s.pods().inNamespace("default").resource(pod).create();
            PodInfo podInfo = new PodInfo(k8s.pods().inNamespace("default").withName(podName).get());
            return gson.toJson(podInfo);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String deletePod(String podName) {
        try {
            if (k8s.pods().inNamespace("default").withName(podName).get() != null) {
                k8s.pods().inNamespace("default").withName(podName).delete();
                HashMap<String, String> successMap = new HashMap<>();
                successMap.put("success", "Pod deleted successfully");
                return gson.toJson(successMap);
            } else {
                HashMap<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Pod doesn't exist");
                return gson.toJson(errorMap);
            }
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

}
