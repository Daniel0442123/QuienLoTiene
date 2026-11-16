package com.quienlotiene.shared.infrastructure.config;

import com.quienlotiene.shared.infrastructure.web.ConvertidorRecordsOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Ajustes del contrato OpenAPI que publica springdoc (D-17, DEF-001). springdoc toma el convertidor como bean. */
@Configuration(proxyBeanMethods = false)
public class ContratoOpenApiConfig {

    @Bean
    ConvertidorRecordsOpenApi convertidorRecordsOpenApi() {
        return new ConvertidorRecordsOpenApi();
    }
}
