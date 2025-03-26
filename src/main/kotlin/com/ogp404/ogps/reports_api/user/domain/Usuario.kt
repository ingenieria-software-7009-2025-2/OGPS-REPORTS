package com.ogp404.ogps.reports_api.user.domain
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User entity representing a registered user in the OGPS Reports system")
data class Usuario(

    /**
     * La anotacion @Schema proporciona metadatos sobre cada campo en la documentacion de Swagger/OpenAPI
     * description: Explica el proposito del campo
     * example: Valor tipico para este campo
     */
    @Schema(
        description = "Unique identifier for the user",
        example = "1",
        required = true // Indica que el campo es obligatorio
    )
    val id: Int = 0,          // Se agrega ID porque en la BD "person" tiene "id_person"

    @Schema(
        description = "Unique username for the user",
        example = "BTCloutt1",
        required = true
    )
    val userName: String = "",    // Coincide con el campo "user_name"

    @Schema(
        description = "User's first name",
        example = "Geo",
        minLength = 2,
        maxLength = 50,
        required = true
    )
    val firstName: String = "",   // Coincide con el campo "first_Name"

    @Schema(
        description = "User's last name",
        example = "Mendez",
        required = true
    )
    val lastName: String = "", // Coincide con el campo "last_Name"

    @Schema(
        description = "User's email address",
        example = "geo_mendez@gmail.com",
        required = true
    )
    val mail: String = "",        // Coincide con el campo "mail"

    @Schema(
        description = "User's password (not returned in responses)",
        example = "securePassword_123",
        required = true,
        writeOnly = true  // Indica que este campo solo es para entrada
    )
    val password: String = "",    // Coincide con el campo "password"

    @Schema(
        description = "Authentication token for the user",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = false
    )
    val token: String? = null,    // Puede ser NULL en la BD

    @Schema(
        description = "User's role in the system",
        example = "User",
        allowableValues = ["User", "Administrator"], // Valores aceptados en este campo
        required = true
    )
    val role: String = "User",    // Puede ser "User" o "Administrator"
)
