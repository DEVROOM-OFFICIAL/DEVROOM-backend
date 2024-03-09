package com.devlatte.devroom.dto;

import org.springframework.http.HttpStatus;

public record NormalResponse(HttpStatus status, String message) {
}
