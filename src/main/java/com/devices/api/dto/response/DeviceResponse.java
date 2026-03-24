package com.devices.api.dto.response;

import com.devices.api.model.DeviceState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private Long id;

    private String name;

    private String brand;

    private DeviceState state;

    private OffsetDateTime creationTime;
}
