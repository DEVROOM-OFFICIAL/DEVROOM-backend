package com.devlatte.devroom.k8s.exception;

public class NoAvailablePortException extends Exception {
    public NoAvailablePortException(String message) {
        super(message);
    }
}