package com.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class PodDto {
    private String name;
    private String status;
    private String namespace;

    @Builder
    public PodDto(String name, String status, String namespace) {
        this.name = name;
        this.status = status;
        this.namespace = namespace;
    }
}
