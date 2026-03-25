package com.devices.api.dto.request;

import com.devices.api.model.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for fully updating a device")
public class UpdateDeviceRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Device name", example = "Galaxy S24")
    private String name;

    @NotBlank(message = "Brand is required")
    @Schema(description = "Device brand", example = "Samsung")
    private String brand;

    @NotNull(message = "State is required")
    @Schema(description = "Device state")
    private DeviceState state;

    @NotNull(message = "Version must be provided for update operations")
    @Schema(description = "Optimistic lock version", example = "0")
    private Long version;
}
