package com.devices.api.dto.request;

import com.devices.api.model.DeviceState;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchDeviceRequest {

    @NotNull(message = "Version must be provided for patch operations")
    private Long version;

    private String name;

    private String brand;

    private DeviceState state;


}
