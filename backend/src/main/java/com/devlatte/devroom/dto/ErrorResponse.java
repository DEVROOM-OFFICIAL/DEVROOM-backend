package com.devlatte.devroom.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponse(HttpStatus status, String message) {
}
