package com.devices.api.controller;

import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.request.PatchDeviceRequest;
import com.devices.api.dto.request.UpdateDeviceRequest;
import com.devices.api.dto.response.DeviceResponse;
import com.devices.api.exception.ErrorResponse;
import com.devices.api.model.DeviceState;
import com.devices.api.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Devices", description = "Device management endpoints")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "Create a new device")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody CreateDeviceRequest request) {
        log.info("Create device request received: name='{}', brand='{}'",
                request.getName(), request.getBrand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.create(request));
    }

    @Operation(summary = "Fully update a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Device in use or optimistic lock conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(
            @Parameter(description = "Device ID") @PathVariable Long id,
            @Valid @RequestBody UpdateDeviceRequest request) {

        log.info("Update device request: id={}, name='{}', brand='{}', state={}, version={}",
                id, request.getName(), request.getBrand(), request.getState(), request.getVersion());

        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @Operation(summary = "Partially update a device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device patched"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Device in use or optimistic lock conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponse> patch(
            @Parameter(description = "Device ID") @PathVariable Long id,
            @Valid @RequestBody PatchDeviceRequest request) {

        log.info("Patch device request: id={}, name={}, brand={}, state={}, version={}",
                id,
                request.getName(),
                request.getBrand(),
                request.getState(),
                request.getVersion());

        return ResponseEntity.ok(deviceService.patch(id, request));
    }

    @Operation(summary = "Get a device by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> findById(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        log.info("Fetch device request: id={}", id);
        return ResponseEntity.ok(deviceService.findById(id));
    }

    @Operation(summary = "List all devices", description = "Optionally filter by brand or state")
    @ApiResponse(responseCode = "200", description = "Device list returned")
    @GetMapping
    public ResponseEntity<List<DeviceResponse>> findAll(
            @Parameter(description = "Filter by brand") @RequestParam(required = false) String brand,
            @Parameter(description = "Filter by state") @RequestParam(required = false) DeviceState state) {

        log.info("Fetch devices request: brand='{}', state={}", brand, state);

        if (brand != null) {
            return ResponseEntity.ok(deviceService.findByBrand(brand));
        }
        if (state != null) {
            return ResponseEntity.ok(deviceService.findByState(state));
        }
        return ResponseEntity.ok(deviceService.findAll());
    }

    @Operation(summary = "Delete a device")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Device is in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        log.info("Delete device request: id={}", id);
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}