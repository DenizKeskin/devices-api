package com.devices.api.dto.response;

import com.devices.api.model.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Device response")
public class DeviceResponse {

    @Schema(description = "Device ID", example = "1")
    private Long id;

    @Schema(description = "Device name", example = "Galaxy S24")
    private String name;

    @Schema(description = "Device brand", example = "Samsung")
    private String brand;

    @Schema(description = "Device state")
    private DeviceState state;

    @Schema(description = "Optimistic lock version", example = "0")
    private Long version;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime creationTime;

    @Schema(description = "Last update timestamp")
    private OffsetDateTime updateTime;
}
