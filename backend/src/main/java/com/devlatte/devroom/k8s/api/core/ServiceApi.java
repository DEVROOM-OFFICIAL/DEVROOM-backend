package com.devlatte.devroom.k8s.api.core;

import com.devlatte.devroom.k8s.model.ServiceInfo;
import io.fabric8.kubernetes.api.model.ServiceBuilder;

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
public class ServiceApi extends K8sApiBase { // Rename class to ServiceApi

    public ServiceApi(@Value("${config.kubernetes.url}") String apiServer,
                      @Value("${config.kubernetes.token}") String apiToken) {
        super(apiServer, apiToken);
    }

    public String getInfo(String label, String value) { // Rename method to getService
        try {
            List<ServiceInfo> services; // Change DeployInfo to ServiceInfo
            if ("all".equals(label)) {
                services = k8s.services().inNamespace("default").list().getItems() // Change deployments() to services()
                        .stream()
                        .map(ServiceInfo::new) // ServiceInfo 객체로 매핑
                        .collect(Collectors.toList());
            } else {
                services = k8s.services().inNamespace("default").withLabel(label, value).list().getItems() // Change deployments() to services()
                        .stream()
                        .map(ServiceInfo::new) // ServiceInfo 객체로 매핑
                        .collect(Collectors.toList());
            }
            return gson.toJson(services);
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String createService(String serviceName, String selector, String exPort, String inPort, Map<String, String> labels) {
        try {

            io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                    .withApiVersion("v1")
                    .withKind("Service")
                    .withNewMetadata()
                    .withName(serviceName)
                    .withLabels(labels)
                    .endMetadata()
                    .withNewSpec()
                    .addNewPort()
                    .withPort(Integer.parseInt(exPort))
                    .withNewTargetPort(Integer.parseInt(inPort))
                    .endPort()
                    .withType("LoadBalancer")
                    .withSelector(Map.of("app", selector))
                    .endSpec()
                    .build();

            k8s.services().inNamespace("default").resource(service).create();
            ServiceInfo serviceInfo = new ServiceInfo(k8s.services().inNamespace("default").withName(serviceName).get());
            return gson.toJson(serviceInfo);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String deleteService(String serviceName) {
        try {
            if (k8s.services().inNamespace("default").withName(serviceName).get() != null) {
                k8s.services().inNamespace("default").withName(serviceName).withGracePeriod(0).delete();
                HashMap<String, String> successMap = new HashMap<>();
                successMap.put("success", "Service deleted successfully");
                return gson.toJson(successMap);
            } else {
                HashMap<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Service doesn't exist");
                return gson.toJson(errorMap);
            }
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }
}
