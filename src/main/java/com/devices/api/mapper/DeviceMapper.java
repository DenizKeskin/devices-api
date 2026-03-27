package com.devices.api.mapper;

import com.devices.api.dto.request.CreateDeviceRequest;
import com.devices.api.dto.response.DeviceResponse;
import com.devices.api.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleteTime", ignore = true)
    Device toEntity(CreateDeviceRequest request);

    DeviceResponse toResponse(Device device);
}
