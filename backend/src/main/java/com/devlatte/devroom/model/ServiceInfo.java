package com.devlatte.devroom.model;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ServiceInfo {
    private String name;
    private String clusterIP;
    private Map<String, String> labels;
    private String creationTimestamp;
    private int port;
    private String type;
    private Map<String, String> selector;
    private String sessionAffinity;
    private List<String> externalIPs;
    private List<String> loadBalancerIngress;
    private List<String> endpointAddresses;

    public ServiceInfo(Service service) {
        this.name = service.getMetadata().getName();
        this.clusterIP = service.getSpec().getClusterIP();
        this.labels = service.getMetadata().getLabels();
        this.creationTimestamp = service.getMetadata().getCreationTimestamp().toString();
        this.port = service.getSpec().getPorts().get(0).getPort(); // 여기서는 첫 번째 포트만 가져옴
        this.type = service.getSpec().getType();
        this.selector = service.getSpec().getSelector();
        this.sessionAffinity = service.getSpec().getSessionAffinity();
        this.externalIPs = service.getSpec().getExternalIPs();
        this.loadBalancerIngress = service.getStatus().getLoadBalancer().getIngress().stream()
                .map(LoadBalancerIngress::getIp) // 각 LoadBalancerIngress 객체에서 IP 주소 추출
                .collect(Collectors.toList());
        this.endpointAddresses = service.getStatus().getLoadBalancer().getIngress().stream()
                .map(loadBalancerIngress -> loadBalancerIngress.getIp())
                .collect(Collectors.toList());
    }
}
