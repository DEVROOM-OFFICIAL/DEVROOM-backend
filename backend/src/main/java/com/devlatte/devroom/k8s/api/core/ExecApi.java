package com.devlatte.devroom.k8s.api.core;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class ExecApi extends K8sApiBase {

    public ExecApi(@Value("${config.kubernetes.url}") String apiServer,
                   @Value("${config.kubernetes.token}") String apiToken) {
        super(apiServer, apiToken);
    }

    public String run(String labelKey, String labelValue, String[] cmd) throws IOException, InterruptedException {


        Map<String, String> result = new HashMap<>();

        try {
            PodList podList = k8s.pods().inNamespace("default").withLabel(labelKey, labelValue).list();

            for (Pod pod : podList.getItems()) {
                String podName = pod.getMetadata().getName();

                String containerName = pod.getSpec().getContainers().getFirst().getName();
//                logger.info("Processing pod: {}", podName);
//                logger.info("Processing container: {}", containerName);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CountDownLatch execLatch = new CountDownLatch(1);

                ExecWatch execWatch = k8s.pods().inNamespace("default").withName(podName).inContainer(containerName).writingOutput(outputStream)
                        .usingListener(new ExecListener() {
                            @Override
                            public void onFailure(Throwable t, Response response) {
                                execLatch.countDown();
                            }

                            @Override
                            public void onClose(int code, String reason) {
                                execLatch.countDown();
                            }
                        }).exec(cmd);

                execLatch.await(10, TimeUnit.SECONDS);
                execWatch.close();
                result.put(podName, outputStream.toString(StandardCharsets.UTF_8));
            }

            return gson.toJson(result);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }
}
