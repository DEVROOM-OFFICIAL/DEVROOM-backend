package com.devlatte.devroom.api;

import com.devlatte.devroom.model.DeployInfo;
import com.devlatte.devroom.model.PodInfo;
import com.devlatte.devroom.model.ServiceInfo;
import com.google.gson.Gson;
import io.fabric8.kubernetes.client.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GetInfoApi {

    @Value("${config.kubernetes.url}")
    private String apiServer;

    @Value("${config.kubernetes.token}")
    private String apiToken;

    private final Gson gson = new Gson();
    private KubernetesClient k8s;

    @PostConstruct
    public void init() {
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(apiToken)
                .withTrustCerts(true)
                .build();
        k8s = new KubernetesClientBuilder().withConfig(config).build();
    }

    public String getDeploy(String label, String value) {
        try {
            List<DeployInfo> deployments;
            if ("all".equals(label)) {
                deployments = k8s.apps().deployments().inNamespace("default").list().getItems()
                        .stream()
                        .map(DeployInfo::new) // DeployInfo 객체로 매핑
                        .collect(Collectors.toList());
            } else {
                deployments = k8s.apps().deployments().inNamespace("default").withLabel(label, value).list().getItems()
                        .stream()
                        .map(DeployInfo::new) // DeployInfo 객체로 매핑
                        .collect(Collectors.toList());
            }
            return gson.toJson(deployments);
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            return "Error occurred while communicating with Kubernetes";
        }
    }

    public String getService(String label, String value) {
        try {
            List<ServiceInfo> services;
            if ("all".equals(label)) {
                services = k8s.services().inNamespace("default").list().getItems()
                        .stream()
                        .map(ServiceInfo::new) // ServiceInfo 객체로 매핑
                        .collect(Collectors.toList());
            } else {
                services = k8s.services().inNamespace("default").withLabel(label, value).list().getItems()
                        .stream()
                        .map(ServiceInfo::new) // ServiceInfo 객체로 매핑
                        .collect(Collectors.toList());
            }
            return gson.toJson(services);
        } catch (KubernetesClientException e) {
            e.printStackTrace();
            return "Error occurred while communicating with Kubernetes";
        }
    }

    public String getPod(String label, String value) {
        try {
            List<PodInfo> podNames;
            if ("all".equals(label)) {
                podNames = k8s.pods().inNamespace("default").list().getItems()
                        .stream()
                        .map(PodInfo::new) // PodInfo 객체로 매핑
                        .collect(Collectors.toList());
            } else {
                podNames = k8s.pods().inNamespace("default").withLabel(label, value).list().getItems()
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

}
