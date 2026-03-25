package com.devices.api.service;


import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.request.PatchDeviceRequest;
import com.devices.api.dto.request.UpdateDeviceRequest;
import com.devices.api.dto.response.DeviceResponse;
import com.devices.api.model.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceService {

    DeviceResponse create(CreateDeviceRequest request);

    DeviceResponse update(Long id, UpdateDeviceRequest request);

    DeviceResponse patch(Long id, PatchDeviceRequest request);

    DeviceResponse findById(Long id);

    Page<DeviceResponse> findAll(Pageable pageable);

    Page<DeviceResponse> findByBrand(String brand, Pageable pageable);

    Page<DeviceResponse> findByState(DeviceState state, Pageable pageable);

    void delete(Long id);
}
