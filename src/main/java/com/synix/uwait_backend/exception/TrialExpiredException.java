package com.synix.uwait_backend.exception;

public class TrialExpiredException extends RuntimeException {
    public TrialExpiredException(String msg) {
        super(msg);
    }
}

