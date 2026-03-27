package com.devices.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new device")
public class CreateDeviceRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Device name", example = "Galaxy S24")
    private String name;

    @NotBlank(message = "Brand is required")
    @Size(max = 255, message = "Brand must not exceed 255 characters")
    @Schema(description = "Device brand", example = "Samsung")
    private String brand;
}
