package com.devices.api.dto.request;

import com.devices.api.model.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for partially updating a device")
public class PatchDeviceRequest {

    @NotNull(message = "Version must be provided for patch operations")
    @Schema(description = "Optimistic lock version", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;

    @Schema(description = "Device name", example = "Galaxy S24")
    private String name;

    @Schema(description = "Device brand", example = "Samsung")
    private String brand;

    @Schema(description = "Device state")
    private DeviceState state;

}
