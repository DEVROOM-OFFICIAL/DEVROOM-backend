package com.devlatte.devroom.k8s.api.core;

import com.devlatte.devroom.k8s.model.PVClaimInfo;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PVClaimApi extends K8sApiBase {

    public PVClaimApi(@Value("${config.kubernetes.url}") String apiServer,
                      @Value("${config.kubernetes.token}") String apiToken) {
        super(apiServer, apiToken);
    }

    public String getInfo(String label, String value) {
        try {
            List<PVClaimInfo> persistentVolumeClaims;
            if ("all".equals(label)) {
                persistentVolumeClaims = k8s.persistentVolumeClaims().inNamespace("default").list().getItems()
                        .stream()
                        .map(PVClaimInfo::new)
                        .collect(Collectors.toList());
            } else {
                persistentVolumeClaims = k8s.persistentVolumeClaims().inNamespace("default").withLabel(label, value).list().getItems()
                        .stream()
                        .map(PVClaimInfo::new)
                        .collect(Collectors.toList());
            }
            return gson.toJson(persistentVolumeClaims);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String createPVClaim(String pvClaimName, String pvCapacity) {
        try {
            Quantity capacityQuantity = new QuantityBuilder()
                    .withAmount(pvCapacity)
                    .build();

            PersistentVolumeClaim pvClaim = new PersistentVolumeClaimBuilder()
                    .withNewMetadata()
                    .withName(pvClaimName + "-claim")
                    .endMetadata()
                    .withNewSpec()
                    .withAccessModes("ReadWriteMany")
                    .withNewResources()
                    .addToRequests("storage", capacityQuantity)
                    .endResources()
                    .withStorageClassName("")
                    .endSpec()
                    .build();

            k8s.persistentVolumeClaims().inNamespace("default").resource(pvClaim).create();
            PVClaimInfo persistentVolumeClaim = new PVClaimInfo(k8s.persistentVolumeClaims().inNamespace("default").withName(pvClaimName+"-claim").get());
            return gson.toJson(persistentVolumeClaim);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String deletePVClaim(String pvClaimName) {
        try {
            if (k8s.persistentVolumeClaims().inNamespace("default").withName(pvClaimName).get() != null) {
                k8s.persistentVolumeClaims().inNamespace("default").withName(pvClaimName).delete();
                HashMap<String, String> successMap = new HashMap<>();
                successMap.put("success", "Persistent Volume Claim deleted successfully");
                return gson.toJson(successMap);
            } else {
                HashMap<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Persistent Volume Claim doesn't exist");
                return gson.toJson(errorMap);
            }
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }
}
