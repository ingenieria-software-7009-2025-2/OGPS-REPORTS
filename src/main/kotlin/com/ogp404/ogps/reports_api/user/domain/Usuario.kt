package com.ogp404.ogps.reports_api.user.domain

data class Usuario(
    val id: Int = 0,          // Se agrega ID porque en la BD "person" tiene "id_person"
    val userName: String = "",    // Coincide con el campo "user_name"
    val firstName: String = "",   // Separado en firstName y lastName
    val lastName: String = "",
    val mail: String = "",        // Coincide con el campo "mail"
    val password: String = "",    // Coincide con el campo "password"
    val token: String? = null,    // Puede ser NULL en la BD
    val role: String = "User",    // Puede ser "User" o "Administrator"
)
