package com.devices.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Devices API",
                version = "1.0.0",
                description = "REST API for managing devices",
                contact = @Contact(name = "Devices Team")
        )
)
@Configuration
public class OpenApiConfig {
}
