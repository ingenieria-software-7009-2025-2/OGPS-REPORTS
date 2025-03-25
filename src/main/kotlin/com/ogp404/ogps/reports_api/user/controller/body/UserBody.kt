package com.ogp404.ogps.reports_api.user.controller.body
import io.swagger.v3.oas.annotations.media.Schema

data class UserBody(

    /**
     * La anotacion @Schema proporciona metadatos sobre cada campo en la documentacion de Swagger/OpenAPI
     * description: Explica el proposito del campo
     * example: Valor tipico para este campo
     */
    @Schema(
        description = "Username for the new user",
        example = "BTCloutt1"
    )
    val userName: String,   // Nombre de usuario (user_name en la BD)

    @Schema(
        description = "First name of the user",
        example = "Geo"
    )
    val firstName: String,  // Nombre (first_name en la BD)

    @Schema(
        description = "Last name of the user",
        example = "Mendez"
    )
    val lastName: String,   // Apellido (last_name en la BD)

    @Schema(
        description = "User's password",
        example = "securePassword_123"
    )
    val password: String,   // Contraseña (password en la BD)

    @Schema(
        description = "User's email address",
        example = "geo_mendez@gmail.com"
    )
    val mail: String,       // Correo electrónico (mail en la BD)

    @Schema(
        description = "User's role in the system",
        example = "User"
    )
    val role: String // Puede ser "User" o "Administrator" (por defecto "User")
)
