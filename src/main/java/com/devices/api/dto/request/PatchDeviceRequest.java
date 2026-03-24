package com.devices.api.dto.request;

import com.devices.api.model.DeviceState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchDeviceRequest {

    private String name;

    private String brand;

    private DeviceState state;
}
