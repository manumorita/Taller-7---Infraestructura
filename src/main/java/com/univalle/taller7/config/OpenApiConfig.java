package com.univalle.taller7.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        Info informacion = new Info();
        informacion.setTitle("Taller 7 - Usuarios");
        informacion.setVersion("1.0.0");
        informacion.setDescription(
                "Creacion y consulta de usuarios. Los emails se almacenan en minusculas.");

        OpenAPI documentacion = new OpenAPI();
        documentacion.setInfo(informacion);
        return documentacion;
    }
}
