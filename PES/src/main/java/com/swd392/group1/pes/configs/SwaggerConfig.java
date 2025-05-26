package com.swd392.group1.pes.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "PES",
                version = "1.0",
                description = "The Preschool Enrollment System"
        ),

        servers = {
                @Server (
                        description = "localhost",
                        url = "http://localhost:8080/"
                )
        }
)
public class SwaggerConfig {

}
