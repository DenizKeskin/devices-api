package com.devices.api.service;

import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.request.PatchDeviceRequest;
import com.devices.api.dto.request.UpdateDeviceRequest;
import com.devices.api.dto.response.DeviceResponse;
import com.devices.api.exception.DeviceInUseException;
import com.devices.api.exception.DeviceNotFoundException;
import com.devices.api.mapper.DeviceMapper;
import com.devices.api.model.Device;
import com.devices.api.model.DeviceState;
import com.devices.api.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Device device = Device.builder().id(1L).name("Old").brand("OldBrand").state(DeviceState.AVAILABLE).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("New", "NewBrand", DeviceState.IN_USE, 0L);
        Device saved = Device.builder().id(1L).name("New").brand("NewBrand").state(DeviceState.IN_USE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).name("New").brand("NewBrand").state(DeviceState.IN_USE).build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(saved);
        when(deviceMapper.toResponse(saved)).thenReturn(expected);

        assertThat(deviceService.update(1L, request)).isEqualTo(expected);
    }

    @Test
    void update_shouldThrowDeviceNotFoundException_whenDeviceNotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.update(99L, new UpdateDeviceRequest("N", "B", DeviceState.AVAILABLE, 0L)))
                .isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    void update_shouldThrowDeviceInUseException_whenInUseAndNameChanged() {
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.IN_USE).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("New", "Brand", DeviceState.IN_USE, 0L);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.update(1L, request))
                .isInstanceOf(DeviceInUseException.class);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDeviceInUseException_whenInUseAndBrandChanged() {
        Device device = Device.builder().id(1L).name("Name").brand("OldBrand").state(DeviceState.IN_USE).build();
        UpdateDeviceRequest request = new UpdateDeviceRequest("Name", "NewBrand", DeviceState.IN_USE, 0L);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.update(1L, request))
                .isInstanceOf(DeviceInUseException.class);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_shouldSucceed_whenInUseButNameAndBrandUnchanged() {
        Device device = Device.builder().id(1L).name("Name").brand("Brand").state(DeviceState.IN_USE).build();
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
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.AVAILABLE).build();
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
        Device device = Device.builder().id(1L).name("Old").brand("Brand").state(DeviceState.IN_USE).build();
        PatchDeviceRequest request = PatchDeviceRequest.builder().version(0L).name("New").build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> deviceService.patch(1L, request))
                .isInstanceOf(DeviceInUseException.class);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void patch_shouldSucceed_whenInUseAndOnlyStateChanged() {
        Device device = Device.builder().id(1L).name("Name").brand("Brand").state(DeviceState.IN_USE).build();
        PatchDeviceRequest request = PatchDeviceRequest.builder().version(0L).state(DeviceState.AVAILABLE).build();
        Device saved = Device.builder().id(1L).name("Name").brand("Brand").state(DeviceState.AVAILABLE).build();
        DeviceResponse expected = DeviceResponse.builder().id(1L).state(DeviceState.AVAILABLE).build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(saved);
        when(deviceMapper.toResponse(saved)).thenReturn(expected);

        assertThat(deviceService.patch(1L, request).getState()).isEqualTo(DeviceState.AVAILABLE);
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
    void findAll_shouldReturnAllDevices() {
        List<Device> devices = List.of(Device.builder().id(1L).build(), Device.builder().id(2L).build());
        List<DeviceResponse> expected = List.of(DeviceResponse.builder().id(1L).build(), DeviceResponse.builder().id(2L).build());

        when(deviceRepository.findAll()).thenReturn(devices);
        when(deviceMapper.toResponseList(devices)).thenReturn(expected);

        assertThat(deviceService.findAll()).hasSize(2);
    }

    @Test
    void findByBrand_shouldReturnFilteredDevices() {
        List<Device> devices = List.of(Device.builder().id(1L).brand("Samsung").build());
        List<DeviceResponse> expected = List.of(DeviceResponse.builder().id(1L).brand("Samsung").build());

        when(deviceRepository.findByBrand("Samsung")).thenReturn(devices);
        when(deviceMapper.toResponseList(devices)).thenReturn(expected);

        List<DeviceResponse> result = deviceService.findByBrand("Samsung");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("Samsung");
    }

    @Test
    void findByState_shouldReturnFilteredDevices() {
        List<Device> devices = List.of(Device.builder().id(1L).state(DeviceState.AVAILABLE).build());
        List<DeviceResponse> expected = List.of(DeviceResponse.builder().id(1L).state(DeviceState.AVAILABLE).build());

        when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(devices);
        when(deviceMapper.toResponseList(devices)).thenReturn(expected);

        assertThat(deviceService.findByState(DeviceState.AVAILABLE)).hasSize(1);
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
