package com.devices.api.exception;

public class DeviceVersionMismatchException extends RuntimeException {

    public DeviceVersionMismatchException(Long provided, Long current) {
        super("Version mismatch: you provided version " + provided +
              " but the current version is " + current + ". Fetch the latest data and retry.");
    }
}
