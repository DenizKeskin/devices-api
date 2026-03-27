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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_shouldSetAvailableStateAndReturnResponse() {
        CreateDeviceRequest request = new CreateDeviceRequest("Galaxy S24", "Samsung");
        Device device = Device.builder().name("Galaxy S24").brand("Samsung").build();
        Device saved = Device.builder().id(1L).name("Galaxy S24").brand("Samsung").state(DeviceState.AVAILABLE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).name("Galaxy S24").brand("Samsung").state(DeviceState.AVAILABLE).build();

        when(deviceMapper.toEntity(request)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(saved);
        when(deviceMapper.toResponse(saved)).thenReturn(expected);

        DeviceResponse result = deviceService.create(request);

        assertThat(result).isEqualTo(expected);
        assertThat(device.getState()).isEqualTo(DeviceState.AVAILABLE);
        verify(deviceRepository).save(device);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateSuccessfully() {
        Device device = Device.builder().id(1L).name("Old").brand("OldBrand").state(DeviceState.AVAILABLE).version(0L).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("New", "NewBrand", DeviceState.IN_USE, 0L);
        Device saved = Device.builder().id(1L).name("New").brand("NewBrand").state(DeviceState.IN_USE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).name("New").brand("NewBrand").state(DeviceState.IN_USE).build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(saved);
        when(deviceMapper.toResponse(saved)).thenReturn(expected);

        assertThat(deviceService.update(1L, request)).isEqualTo(expected);
    }

    @Test
    void update_shouldThrowDeviceVersionMismatchException_whenVersionMismatch() {
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.AVAILABLE).version(3L).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("New", "Brand", DeviceState.AVAILABLE, 0L);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.update(1L, request))
                .isInstanceOf(DeviceVersionMismatchException.class)
                .hasMessageContaining("provided version 0")
                .hasMessageContaining("current version is 3");
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDeviceNotFoundException_whenDeviceNotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.update(99L, new UpdateDeviceRequest("N", "B", DeviceState.AVAILABLE, 0L)))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    void update_shouldThrowDeviceInUseException_whenInUseAndNameChanged() {
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.IN_USE).version(0L).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("New", "Brand", DeviceState.IN_USE, 0L);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.update(1L, request))
                .isInstanceOf(DeviceInUseException.class);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDeviceInUseException_whenInUseAndBrandChanged() {
        Device device = Device.builder().id(1L).name("Name").brand("OldBrand").state(DeviceState.IN_USE).version(0L).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("Name", "NewBrand", DeviceState.IN_USE, 0L);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.update(1L, request))
                .isInstanceOf(DeviceInUseException.class);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldSucceed_whenInUseButNameAndBrandUnchanged() {
        Device device = Device.builder().id(1L).name("Name").brand("Brand").state(DeviceState.IN_USE).version(0L).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("Name", "Brand", DeviceState.INACTIVE, 0L);
        Device saved = Device.builder().id(1L).name("Name").brand("Brand").state(DeviceState.INACTIVE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).state(DeviceState.INACTIVE).build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(saved);
        when(deviceMapper.toResponse(saved)).thenReturn(expected);

        assertThat(deviceService.update(1L, request).getState()).isEqualTo(DeviceState.INACTIVE);
    }

    // ── patch ─────────────────────────────────────────────────────────────────

    @Test
    void patch_shouldApplyPartialUpdate() {
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.AVAILABLE).version(0L).build();
        PatchDeviceRequest request = PatchDeviceRequest.builder().version(0L).name("New").build();
        Device saved = Device.builder().id(1L).name("New").brand("Brand").state(DeviceState.AVAILABLE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).name("New").build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(saved);
        when(deviceMapper.toResponse(saved)).thenReturn(expected);

        assertThat(deviceService.patch(1L, request)).isEqualTo(expected);
    }

    @Test
    void patch_shouldThrowDeviceInUseException_whenInUseAndNameChanged() {
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.IN_USE).version(0L).build();
        PatchDeviceRequest request = PatchDeviceRequest.builder().version(0L).name("New").build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.patch(1L, request))
                .isInstanceOf(DeviceInUseException.class);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void patch_shouldSucceed_whenInUseAndOnlyStateChanged() {
        Device device = Device.builder().id(1L).name("Name").brand("Brand").state(DeviceState.IN_USE).version(0L).build();
        PatchDeviceRequest request = PatchDeviceRequest.builder().version(0L).state(DeviceState.AVAILABLE).build();
        Device saved = Device.builder().id(1L).name("Name").brand("Brand").state(DeviceState.AVAILABLE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).state(DeviceState.AVAILABLE).build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(saved);
        when(deviceMapper.toResponse(saved)).thenReturn(expected);

        assertThat(deviceService.patch(1L, request).getState()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void patch_shouldThrowDeviceVersionMismatchException_whenVersionMismatch() {
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.AVAILABLE).version(3L).build();
        PatchDeviceRequest request = PatchDeviceRequest.builder().version(0L).name("New").build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.patch(1L, request))
                .isInstanceOf(DeviceVersionMismatchException.class)
                .hasMessageContaining("provided version 0")
                .hasMessageContaining("current version is 3");
        verify(deviceRepository, never()).save(any());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnDeviceResponse() {
        Device device = Device.builder().id(1L).name("Galaxy").brand("Samsung").state(DeviceState.AVAILABLE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).name("Galaxy").build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceMapper.toResponse(device)).thenReturn(expected);

        assertThat(deviceService.findById(1L)).isEqualTo(expected);
    }

    @Test
    void findById_shouldThrowDeviceNotFoundException() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.findById(99L))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    // ── findAll / findByBrand / findByState ───────────────────────────────────

    @Test
    void findAll_shouldReturnPagedDevices() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Device> devices = List.of(Device.builder().id(1L).build(), Device.builder().id(2L).build());
        Page<Device> devicePage = new PageImpl<>(devices, pageable, 2);

        when(deviceRepository.findAll(pageable)).thenReturn(devicePage);
        when(deviceMapper.toResponse(any(Device.class))).thenReturn(DeviceResponse.builder().id(1L).build());

        Page<DeviceResponse> result = deviceService.findAll(pageable);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByBrand_shouldReturnFilteredPagedDevices() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Device> devices = List.of(Device.builder().id(1L).brand("Samsung").build());
        Page<Device> devicePage = new PageImpl<>(devices, pageable, 1);

        when(deviceRepository.findByBrand("Samsung", pageable)).thenReturn(devicePage);
        when(deviceMapper.toResponse(any(Device.class))).thenReturn(DeviceResponse.builder().id(1L).brand("Samsung").build());

        Page<DeviceResponse> result = deviceService.findByBrand("Samsung", pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getBrand()).isEqualTo("Samsung");
    }

    @Test
    void findByState_shouldReturnFilteredPagedDevices() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Device> devices = List.of(Device.builder().id(1L).state(DeviceState.AVAILABLE).build());
        Page<Device> devicePage = new PageImpl<>(devices, pageable, 1);

        when(deviceRepository.findByState(DeviceState.AVAILABLE, pageable)).thenReturn(devicePage);
        when(deviceMapper.toResponse(any(Device.class))).thenReturn(DeviceResponse.builder().id(1L).state(DeviceState.AVAILABLE).build());

        Page<DeviceResponse> result = deviceService.findByState(DeviceState.AVAILABLE, pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_shouldDeleteSuccessfully() {
        Device device = Device.builder().id(1L).state(DeviceState.AVAILABLE).build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        deviceService.delete(1L);

        verify(deviceRepository).delete(device);
    }

    @Test
    void delete_shouldThrowDeviceNotFoundException() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.delete(99L))
                .isInstanceOf(DeviceNotFoundException.class);
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowDeviceInUseException_whenDeviceIsInUse() {
        Device device = Device.builder().id(1L).state(DeviceState.IN_USE).build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.delete(1L))
                .isInstanceOf(DeviceInUseException.class);
        verify(deviceRepository, never()).delete(any());
    }
}
