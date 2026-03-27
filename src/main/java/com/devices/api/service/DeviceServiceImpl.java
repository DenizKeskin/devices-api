package com.devices.api.service;

import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.request.PatchDeviceRequest;
import com.devices.api.dto.request.UpdateDeviceRequest;
import com.devices.api.dto.response.DeviceResponse;
import com.devices.api.exception.DeviceInUseException;
import com.devices.api.exception.DeviceNotFoundException;
import com.devices.api.exception.DeviceVersionMismatchException;
import com.devices.api.mapper.DeviceMapper;
import com.devices.api.model.Device;
import com.devices.api.model.DeviceState;
import com.devices.api.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Override
    @Transactional
    public DeviceResponse create(CreateDeviceRequest request) {
        Device device = deviceMapper.toEntity(request);
        device.setState(DeviceState.AVAILABLE);
        Device saved = deviceRepository.save(device);
        log.info("Device created with id={}", saved.getId());
        return deviceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse update(Long id, UpdateDeviceRequest request) {
        Device device = findDeviceOrThrow(id);

        if (!Objects.equals(request.getVersion(), device.getVersion())) {
            throw new DeviceVersionMismatchException(request.getVersion(), device.getVersion());
        }

        boolean isNameChanged = !Objects.equals(device.getName(), request.getName());
        boolean isBrandChanged = !Objects.equals(device.getBrand(), request.getBrand());

        if (Objects.equals(DeviceState.IN_USE, device.getState()) && (isNameChanged || isBrandChanged)) {
            throw new DeviceInUseException("Name or brand cannot be updated while device is IN_USE");
        }

        device.setName(request.getName());
        device.setBrand(request.getBrand());
        device.setState(request.getState());

        Device saved = deviceRepository.save(device);
        log.info("Device id={} fully updated", id);
        return deviceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse patch(Long id, PatchDeviceRequest request) {
        Device device = findDeviceOrThrow(id);

        if (!Objects.equals(request.getVersion(), device.getVersion())) {
            throw new DeviceVersionMismatchException(request.getVersion(), device.getVersion());
        }

        validatePatchRequest(device, request);
        applyPatch(device, request);

        Device saved = deviceRepository.save(device);
        log.info("Device id={} partially updated", id);

        return deviceMapper.toResponse(saved);
    }

    private void validatePatchRequest(Device device, PatchDeviceRequest request) {
        boolean isNameChanged =
                request.getName() != null && !Objects.equals(device.getName(), request.getName());

        boolean isBrandChanged =
                request.getBrand() != null && !Objects.equals(device.getBrand(), request.getBrand());

        if (Objects.equals(DeviceState.IN_USE, device.getState()) && (isNameChanged || isBrandChanged)) {
            throw new DeviceInUseException("Name or brand cannot be updated while device is IN_USE");
        }
    }

    private void applyPatch(Device device, PatchDeviceRequest request) {
        if (request.getName() != null) {
            device.setName(request.getName());
        }
        if (request.getBrand() != null) {
            device.setBrand(request.getBrand());
        }
        if (request.getState() != null) {
            device.setState(request.getState());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse findById(Long id) {
        return deviceMapper.toResponse(findDeviceOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> findAll(Pageable pageable) {
        return deviceRepository.findAll(pageable).map(deviceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> findByBrand(String brand, Pageable pageable) {
        return deviceRepository.findByBrand(brand, pageable).map(deviceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceResponse> findByState(DeviceState state, Pageable pageable) {
        return deviceRepository.findByState(state, pageable).map(deviceMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Device device = findDeviceOrThrow(id);

        if (Objects.equals(DeviceState.IN_USE, device.getState())) {
            throw new DeviceInUseException("Device cannot be deleted while it is IN_USE");
        }

        deviceRepository.delete(device);
        log.info("Device id={} deleted", id);
    }

    private Device findDeviceOrThrow(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }
}