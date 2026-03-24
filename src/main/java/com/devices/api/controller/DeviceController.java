package com.devices.api.controller;

import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.request.PatchDeviceRequest;
import com.devices.api.dto.request.UpdateDeviceRequest;
import com.devices.api.dto.response.DeviceResponse;
import com.devices.api.model.DeviceState;
import com.devices.api.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody CreateDeviceRequest request) {
        log.info("Create device request received: name='{}', brand='{}'",
                request.getName(), request.getBrand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeviceRequest request) {

        log.info("Update device request: id={}, name='{}', brand='{}', state={}, version={}",
                id, request.getName(), request.getBrand(), request.getState(), request.getVersion());

        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponse> patch(
            @PathVariable Long id,
            @Valid @RequestBody PatchDeviceRequest request) {

        log.info("Patch device request: id={}, name={}, brand={}, state={}, version={}",
                id,
                request.getName(),
                request.getBrand(),
                request.getState(),
                request.getVersion());

        return ResponseEntity.ok(deviceService.patch(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> findById(@PathVariable Long id) {
        log.info("Fetch device request: id={}", id);
        return ResponseEntity.ok(deviceService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> findAll(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) DeviceState state) {

        log.info("Fetch devices request: brand='{}', state={}", brand, state);

        if (brand != null) {
            return ResponseEntity.ok(deviceService.findByBrand(brand));
        }
        if (state != null) {
            return ResponseEntity.ok(deviceService.findByState(state));
        }
        return ResponseEntity.ok(deviceService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Delete device request: id={}", id);
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}