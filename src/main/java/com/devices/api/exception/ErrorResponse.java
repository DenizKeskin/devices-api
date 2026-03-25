package com.devices.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error message", example = "Device not found with id: 1")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/devices/1")
    private String path;

    @Schema(description = "Timestamp of the error")
    private OffsetDateTime timestamp;

    @Schema(description = "Field-level validation errors")
    private Map<String, String> validationErrors;
}
