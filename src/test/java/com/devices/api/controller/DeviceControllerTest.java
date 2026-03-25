package com.devices.api.controller;

import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.request.PatchDeviceRequest;
import com.devices.api.dto.request.UpdateDeviceRequest;
import com.devices.api.model.Device;
import com.devices.api.model.DeviceState;
import com.devices.api.repository.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DeviceControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
    }

    private Device saveDevice(String name, String brand, DeviceState state) {
        return deviceRepository.save(Device.builder()
                .name(name)
                .brand(brand)
                .state(state)
                .build());
    }

    // ── POST /api/v1/devices ──────────────────────────────────────────────────

    @Test
    void createDevice_shouldReturn201_withAvailableState() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("Galaxy S24", "Samsung");

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Galaxy S24"))
                .andExpect(jsonPath("$.brand").value("Samsung"))
                .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    @Test
    void createDevice_shouldReturn400_whenNameIsBlank() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("", "Samsung");

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void createDevice_shouldReturn400_whenBrandIsBlank() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("Galaxy S24", "");

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.brand").exists());
    }

    // ── GET /api/v1/devices/{id} ──────────────────────────────────────────────

    @Test
    void getDevice_shouldReturn200() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);

        mockMvc.perform(get("/api/v1/devices/{id}", device.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(device.getId()))
                .andExpect(jsonPath("$.name").value("Galaxy S24"))
                .andExpect(jsonPath("$.brand").value("Samsung"));
    }

    @Test
    void getDevice_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/devices/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── GET /api/v1/devices ───────────────────────────────────────────────────

    @Test
    void getAllDevices_shouldReturnAllDevices() throws Exception {
        saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);
        saveDevice("iPhone 15", "Apple", DeviceState.IN_USE);

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getDevicesByBrand_shouldReturnFilteredDevices() throws Exception {
        saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);
        saveDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);

        mockMvc.perform(get("/api/v1/devices").param("brand", "Samsung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand").value("Samsung"));
    }

    @Test
    void getDevicesByState_shouldReturnFilteredDevices() throws Exception {
        saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);
        saveDevice("iPhone 15", "Apple", DeviceState.IN_USE);

        mockMvc.perform(get("/api/v1/devices").param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].state").value("AVAILABLE"));
    }

    // ── PUT /api/v1/devices/{id} ──────────────────────────────────────────────

    @Test
    void updateDevice_shouldReturn200() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);
        UpdateDeviceRequest request = new UpdateDeviceRequest("Galaxy S25", "Samsung", DeviceState.IN_USE, device.getVersion());

        mockMvc.perform(put("/api/v1/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Galaxy S25"))
                .andExpect(jsonPath("$.state").value("IN_USE"));
    }

    @Test
    void updateDevice_shouldReturn404_whenNotFound() throws Exception {
        UpdateDeviceRequest request = new UpdateDeviceRequest("Name", "Brand", DeviceState.AVAILABLE, 0L);

        mockMvc.perform(put("/api/v1/devices/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDevice_shouldReturn409_whenInUseAndNameChanged() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
        UpdateDeviceRequest request = new UpdateDeviceRequest("Galaxy S25", "Samsung", DeviceState.IN_USE, device.getVersion());

        mockMvc.perform(put("/api/v1/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateDevice_shouldReturn409_whenInUseAndBrandChanged() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
        UpdateDeviceRequest request = new UpdateDeviceRequest("Galaxy S24", "Apple", DeviceState.IN_USE, device.getVersion());

        mockMvc.perform(put("/api/v1/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/v1/devices/{id} ────────────────────────────────────────────

    @Test
    void patchDevice_shouldReturn200() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);
        PatchDeviceRequest request = PatchDeviceRequest.builder()
                .version(device.getVersion())
                .name("Galaxy S25")
                .build();

        mockMvc.perform(patch("/api/v1/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Galaxy S25"))
                .andExpect(jsonPath("$.brand").value("Samsung"));
    }

    @Test
    void patchDevice_shouldReturn409_whenInUseAndNameChanged() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
        PatchDeviceRequest request = PatchDeviceRequest.builder()
                .version(device.getVersion())
                .name("Galaxy S25")
                .build();

        mockMvc.perform(patch("/api/v1/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void patchDevice_shouldReturn200_whenInUseAndOnlyStateChanged() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
        PatchDeviceRequest request = PatchDeviceRequest.builder()
                .version(device.getVersion())
                .state(DeviceState.AVAILABLE)
                .build();

        mockMvc.perform(patch("/api/v1/devices/{id}", device.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    // ── DELETE /api/v1/devices/{id} ───────────────────────────────────────────

    @Test
    void deleteDevice_shouldReturn204() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);

        mockMvc.perform(delete("/api/v1/devices/{id}", device.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDevice_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/devices/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDevice_shouldReturn409_whenDeviceIsInUse() throws Exception {
        Device device = saveDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);

        mockMvc.perform(delete("/api/v1/devices/{id}", device.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
