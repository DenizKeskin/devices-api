package com.devices.api.exception;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(Long id) {
        super("Device with id " + id + " was not found");
    }
}

