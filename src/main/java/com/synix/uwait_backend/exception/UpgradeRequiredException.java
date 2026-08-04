package com.synix.uwait_backend.exception;

public class UpgradeRequiredException extends RuntimeException {
    public UpgradeRequiredException(String message) {
        super(message);
    }
}

