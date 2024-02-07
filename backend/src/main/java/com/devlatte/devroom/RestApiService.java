package com.devlatte.devroom;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RestApiService {

    @Value("${config.kubernetes.url}")
    private String apiServer;

    @Value("${config.kubernetes.token}")
    private String apiToken;

    private static String API_SERVER;
    private static String API_TOKEN;

    @PostConstruct
    public void init() {
        API_SERVER = apiServer;
        API_TOKEN = apiToken;
    }

    public static String getResource() {

        Config config = new ConfigBuilder()
                .withMasterUrl(API_SERVER)
                .withOauthToken(API_TOKEN)
                .withTrustCerts(true)
                .build();

        try (final KubernetesClient k8s = new KubernetesClientBuilder().withConfig(config).build()) {
            return k8s.pods().inNamespace("default").list().getItems()
                    .stream()
                    .map(pod -> pod.getMetadata().getName())
                    .collect(Collectors.toList())
                    .toString();

        }
    }

}
