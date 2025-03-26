package com.ogp404.ogps.reports_api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuracion de Swagger para la documentacion de la API.
 *
 * Esta clase configura Swagger utilizando SpringDoc OpenAPI para generar
 * automaticamente la documentacion de la API y hacerla accesible en una
 * interfaz visual (Swagger UI).
 */
@Configuration
class SwaggerConfig {

    /**
     * Configura el grupo de API pública.
     *
     * @return Un `GroupedOpenApi` que agrupa todos los endpoints que coinciden
     * con el patron de ruta 'v1/..' permitiendo su documentacion en Swagger.
     */
    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("ogps-reports-api")
            .pathsToMatch("/v1/**")
            .build()
    }

    /**
     * Configura los metadatos generales de la API para Swagger.
     *
     * @return Un objeto `OpenAPI` con la informacion de la API, como su titulo,
     * descripcion y version.
     */
    @Bean
    fun springShopOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("OGPS Reports API")
                    .description("API for a collaborative web platform designed to facilitate citizen participation in urban problem identification and tracking.")
                    .version("v1.0.0")
            )
    }
}