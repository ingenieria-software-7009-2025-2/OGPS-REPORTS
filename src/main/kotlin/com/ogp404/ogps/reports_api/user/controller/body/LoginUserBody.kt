package com.ogp404.ogps.reports_api.user.controller.body
import io.swagger.v3.oas.annotations.media.Schema

data class LoginUserBody(

    /**
     * La anotacion @Schema proporciona metadatos sobre cada campo en la documentacion de Swagger/OpenAPI
     * description: Explica el proposito del campo
     * example: Valor tipico para este campo
    */
    @Schema(
        description = "Email used for login",
        example = "geo_mendez@gmail.com"
    )
    var mail: String, // Correo electronico del usuario

    @Schema(
        description = "User's password",
        example = "securePassword_123"
    )
    var password: String // Contraseña del usuario
)