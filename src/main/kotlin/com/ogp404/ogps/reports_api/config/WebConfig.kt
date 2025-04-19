package com.ogp404.ogps.reports_api.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

// Configuración de Spring para servir archivos estáticos desde la carpeta "uploads" en la ruta "/uploads/**"
// Esto nos sirve para guardar las imagenes en local.
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadDir = Paths.get("uploads").toAbsolutePath().toString()
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadDir/")
    }
}