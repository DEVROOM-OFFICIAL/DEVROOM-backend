package com.devlatte.devroom.k8s.api.basic;

import com.devlatte.devroom.k8s.model.PVInfo;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PVApi extends K8sApiBase {

    public PVApi(@Value("${config.kubernetes.url}") String apiServer,
                 @Value("${config.kubernetes.token}") String apiToken) {
        super(apiServer, apiToken);
    }

    public String getInfo(String label, String value) {
        try {
            List<PVInfo> persistentVolumes;
            if ("all".equals(label)) {
                persistentVolumes = k8s.persistentVolumes().list().getItems()
                        .stream()
                        .map(PVInfo::new)
                        .collect(Collectors.toList());
            } else {
                persistentVolumes = k8s.persistentVolumes().withLabel(label, value).list().getItems()
                        .stream()
                        .map(PVInfo::new)
                        .collect(Collectors.toList());
            }
            return gson.toJson(persistentVolumes);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String createPV(String pvName, String pvCapacity, String nodeName) {
        try {
            Quantity capacityQuantity = new QuantityBuilder()
                    .withAmount(pvCapacity)
                    .build();

            PersistentVolume pv = new PersistentVolumeBuilder()
                    .withNewMetadata()
                    .withName(pvName)
                    .endMetadata()
                    .withNewSpec()
                    .withCapacity(Collections.singletonMap("storage", capacityQuantity))
                    .withAccessModes("ReadWriteMany")
                    .withNewLocal()
                    .withPath("/")
                    .endLocal()
                    .withNewNodeAffinity()
                    .withNewRequired()
                    .addNewNodeSelectorTerm()
                    .addNewMatchExpression()
                    .withKey("storage")
                    .withOperator("In")
                    .withValues(nodeName)
                    .endMatchExpression()
                    .endNodeSelectorTerm()
                    .endRequired()
                    .endNodeAffinity()
                    .endSpec()
                    .build();

            k8s.persistentVolumes().resource(pv).create();

            PVInfo persistentVolume = new PVInfo(k8s.persistentVolumes().withName(pvName).get());
            return gson.toJson(persistentVolume);

        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

    public String deletePV(String pvName) {
        try {
            if (k8s.persistentVolumes().withName(pvName).get() != null) {
                k8s.persistentVolumes().withName(pvName).delete();
                HashMap<String, String> successMap = new HashMap<>();
                successMap.put("success", "Persistent Volume deleted successfully");
                return gson.toJson(successMap);
            } else {
                HashMap<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Persistent Volume doesn't exist");
                return gson.toJson(errorMap);
            }
        } catch (KubernetesClientException e) {
            HashMap<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return gson.toJson(errorMap);
        }
    }

}
