package com.devices.api.service;


import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.request.PatchDeviceRequest;
import com.devices.api.dto.request.UpdateDeviceRequest;
import com.devices.api.dto.response.DeviceResponse;
import com.devices.api.model.DeviceState;

import java.util.List;

public interface DeviceService {

    DeviceResponse create(CreateDeviceRequest request);

    DeviceResponse update(Long id, UpdateDeviceRequest request);

    DeviceResponse patch(Long id, PatchDeviceRequest request);

    DeviceResponse findById(Long id);

    List<DeviceResponse> findAll();

    List<DeviceResponse> findByBrand(String brand);

    List<DeviceResponse> findByState(DeviceState state);

    void delete(Long id);
}
