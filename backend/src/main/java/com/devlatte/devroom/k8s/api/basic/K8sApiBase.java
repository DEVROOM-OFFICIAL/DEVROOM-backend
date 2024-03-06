package com.devlatte.devroom.k8s.api.basic;

import com.devlatte.devroom.k8s.utils.FreemarkerTemplate;
import com.google.gson.Gson;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class K8sApiBase {
    protected final KubernetesClient k8s;
    protected final Gson gson = new Gson();
    protected final Logger logger = LoggerFactory.getLogger(K8sApiBase.class);

    @Value("${config.kubernetes.token}")
    protected String apiServer;
    @Value("${config.kubernetes.token}")
    protected String apiToken;

    public K8sApiBase(String apiServer, String apiToken) {
        this.apiServer = apiServer;
        this.apiToken = apiToken;
        Config config = new ConfigBuilder()
                .withMasterUrl(apiServer)
                .withOauthToken(apiToken)
                .withTrustCerts(true)
                .build();
        k8s = new KubernetesClientBuilder().withConfig(config).build();
    }
}